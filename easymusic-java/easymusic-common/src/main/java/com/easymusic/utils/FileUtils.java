package com.easymusic.utils;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.enums.DateTimePatternEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class FileUtils {
    @Resource
    private AppConfig appConfig;

    public String uploadFile(MultipartFile file, String folderName, String fileName) {
        if (StringTools.isEmpty(folderName)) {
            folderName = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern()) + "/";
        }
        String avatarFolderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + folderName;
        File folder = new File(avatarFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (StringTools.isEmpty(fileName)) {
            fileName = System.currentTimeMillis() + StringTools.getFileSuffix(file.getOriginalFilename());
        }
        try {
            file.transferTo(new File(avatarFolderPath + fileName));
        } catch (IOException e) {
            log.error("图片上传失败", e);
        }
        return folderName + fileName;
    }

    public String copyAvatar(String userId) {
        try {
            int randomNumber = (int) (Math.random() * 20) + 1;
            String avatarFolderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
            File folder = new File(avatarFolderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String avatarPath = Constants.FILE_FOLDER_AVATAR_NAME + userId + Constants.AVATAR_SUFIX;
            File avatarFile = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + avatarPath);
            ClassPathResource classPathResource = new ClassPathResource(String.format(Constants.DEFAULT_AVATAR_PATH, randomNumber));
            org.apache.commons.io.FileUtils.copyToFile(classPathResource.getInputStream(), avatarFile);
            return avatarPath;
        } catch (Exception e) {
            log.error("拷贝头像失败", e);
        }
        return null;
    }

    public String downloadFile(String url, String suffix) {
        String folderName = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        String avatarFolderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + folderName + "/";
        File folder = new File(avatarFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = StringTools.getRandomString(Constants.LENGTH_30) + suffix;
        String filePath = avatarFolderPath + fileName;
        OKHttpUtils.download(url, filePath);
        return folderName + "/" + fileName;
    }
}
