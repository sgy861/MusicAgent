package com.easymusic.controller;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.enums.ResponseCodeEnum;
import com.easymusic.exception.BusinessException;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Date;


@Slf4j
@Validated
@RestController
@RequestMapping("/file")
public class FileController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response,
                            @RequestHeader(required = false, name = "range") String range,
                            @NotEmpty String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + filePath;
        String fileSuffix = StringTools.getFileSuffix(filePath);
        if (!StringTools.isEmpty(fileSuffix) && ArrayUtils.contains(Constants.IMAGES_SUFFIX, fileSuffix.toLowerCase())) {
            //缓存30天
            response.setHeader("Cache-Control", "max-age=" + 30 * 24 * 60 * 60);
            response.setContentType("image/jpg");
        }
        readFile(response, range, filePath);
    }

    protected void readFile(HttpServletResponse response, String range, String filePath) {
        File file = new File(filePath);
        try (ServletOutputStream out = response.getOutputStream();) {
            RandomAccessFile randomFile = new RandomAccessFile(file, "r");//只读模式
            long contentLength = randomFile.length();
            int start = 0, end = 0;
            if (range != null && range.startsWith("bytes=")) {
                String[] values = range.split("=")[1].split("-");
                start = Integer.parseInt(values[0]);
                if (values.length > 1) {
                    end = Integer.parseInt(values[1]);
                }
            }
            int requestSize = 0;
            if (end != 0 && end > start) {
                requestSize = end - start + 1;
            } else {
                requestSize = Integer.MAX_VALUE;
            }

            byte[] buffer = new byte[4096];
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Last-Modified", new Date().toString());
            //第一次请求只返回content length来让客户端请求多次实际数据
            if (range == null) {
                response.setHeader("Content-length", contentLength + "");
            } else {
                //以后的多次以断点续传的方式来返回视频数据
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);//206
                long requestStart = 0, requestEnd = 0;
                String[] ranges = range.split("=");
                if (ranges.length > 1) {
                    String[] rangeDatas = ranges[1].split("-");
                    requestStart = Integer.parseInt(rangeDatas[0]);
                    if (rangeDatas.length > 1) {
                        requestEnd = Integer.parseInt(rangeDatas[1]);
                    }
                }
                long length = 0;
                if (requestEnd > 0) {
                    length = requestEnd - requestStart + 1;
                    response.setHeader("Content-length", "" + length);
                    response.setHeader("Content-Range", "bytes " + requestStart + "-" + requestEnd + "/" + contentLength);
                } else {
                    length = contentLength - requestStart;
                    response.setHeader("Content-length", "" + length);
                    response.setHeader("Content-Range", "bytes " + requestStart + "-" + (contentLength - 1) + "/" + contentLength);
                }
            }
            int needSize = requestSize;
            randomFile.seek(start);
            while (needSize > 0) {
                int len = randomFile.read(buffer);
                if (needSize < buffer.length) {
                    out.write(buffer, 0, needSize);
                } else {
                    out.write(buffer, 0, len);
                    if (len < buffer.length) {
                        break;
                    }
                }
                needSize -= buffer.length;
            }
            randomFile.close();
        } catch (Exception e) {
            log.error("读取文件信息失败", e);
        }
    }
}
