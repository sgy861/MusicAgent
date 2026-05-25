package com.easymusic.utils;

import com.easymusic.entity.enums.ResponseCodeEnum;
import com.easymusic.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OKHttpUtils {
    /**
     * 请求超时时间5秒
     */
    private static final int TIME_OUT_SECONDS = 8;

    private static OkHttpClient.Builder getClientBuilder() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().followRedirects(false).retryOnConnectionFailure(false);
        clientBuilder.connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS).readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        return clientBuilder;
    }

    private static Request.Builder getRequestBuilder(Map<String, String> header) {
        Request.Builder requestBuilder = new Request.Builder();
        if (null != header) {
            for (Map.Entry<String, String> map : header.entrySet()) {
                String key = map.getKey();
                String value;
                if (map.getValue() == null) {
                    value = "";
                } else {
                    value = map.getValue();
                }
                requestBuilder.addHeader(key, value);
            }
        }
        requestBuilder.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
        return requestBuilder;
    }


    public static String getRequest(String url, Map<String, String> header) throws BusinessException {
        ResponseBody responseBody = null;
        String responseStr = null;
        try {
            OkHttpClient.Builder clientBuilder = getClientBuilder();
            OkHttpClient client = clientBuilder.build();
            Request.Builder requestBuilder = getRequestBuilder(header);
            Request request = requestBuilder.url(url).build();
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            responseStr = responseBody.string();
            return responseStr;
        } catch (SocketTimeoutException | ConnectException e) {
            log.error("OKhttpGetRequest请求超时,url:{}", url, e);
            throw new BusinessException(ResponseCodeEnum.CODE_604);
        } catch (Exception e) {
            log.error("OKhttpGetRequest请求异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_605);
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
            log.info("请求地址:{},返回:{}", url, responseStr);
        }
    }

    public static String postRequest4Json(String url, Map<String, String> header, String params) throws BusinessException {
        ResponseBody responseBody = null;
        String responseStr = "";
        try {
            OkHttpClient.Builder clientBuilder = getClientBuilder();
            OkHttpClient client = clientBuilder.build();
            Request.Builder requestBuilder = new Request.Builder();
            Request.Builder builder = requestBuilder.url(url);
            if (null != header) {
                for (Map.Entry<String, String> map : header.entrySet()) {
                    String key = map.getKey();
                    String value;
                    if (map.getValue() == null) {
                        value = "";
                    } else {
                        value = map.getValue();
                    }
                    builder.addHeader(key, value);
                }
            }
            MediaType data = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(data, params);
            Request request = builder.post(requestBody).build();
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            responseStr = responseBody.string();
            return responseStr;
        } catch (SocketTimeoutException | ConnectException e) {
            throw new BusinessException(ResponseCodeEnum.CODE_604);
        } catch (Exception e) {
            throw new BusinessException(ResponseCodeEnum.CODE_605);
        } finally {
            log.info("请求地址:{},参数:{},返回:{}", url, params, responseStr);
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }


    public static void download(String url, String filePath) {
        ResponseBody responseBody = null;
        String responseStr = null;
        try {
            OkHttpClient.Builder clientBuilder = getClientBuilder();
            OkHttpClient client = clientBuilder.build();

            Request.Builder requestBuilder = getRequestBuilder(null);
            Request request = requestBuilder.url(url).build();
            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Response body is null");
            }
            try (InputStream in = body.byteStream(); OutputStream out = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
        } catch (SocketTimeoutException | ConnectException e) {
            log.error("OKhttpGetRequest请求超时,url:{}", url, e);
            throw new BusinessException(ResponseCodeEnum.CODE_604);
        } catch (Exception e) {
            log.error("OKhttpGetRequest请求异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_605);
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
            log.info("请求地址:{},参数:{},返回:{}", url, responseStr, responseStr);
        }
    }
}