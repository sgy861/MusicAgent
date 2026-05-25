package com.easymusic.api;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.PayOrderNotifyDTO;
import com.easymusic.entity.enums.DateTimePatternEnum;
import com.easymusic.entity.enums.PayOrderTypeEnum;
import com.easymusic.entity.enums.PayTypeEnum;
import com.easymusic.exception.BusinessException;
import com.easymusic.utils.DateUtil;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.OKHttpUtils;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@Service("payChannel4Wechat")
@Slf4j
public class PayChannel4WechatImpl implements PayChannelService {

    private static final String CERTIFICATES_URL = "/v3/certificates";

    private static final String PAY_URL = "/v3/pay/transactions/native";

    private static final String QUERY_URL = "/v3/pay/transactions/out-trade-no/%s?mchid=%s";

    private static final String CURRENCY = "CNY";

    private static final String URL_NOTIFY = "/api/payNotify/payNotify4Wechat/%d";

    private static final String RETURN_CODE_SUCCESS = "SUCCESS";

    @Resource
    private AppConfig appConfig;

    @Override
    public String getPayUrl(String orderId, BigDecimal amount, String productName) {
        Map<String, Object> paramsMap = new HashMap<>();
        String appId = appConfig.getPayWechatAppId();
        String mchId = appConfig.getPayWechatMchid();
        String serialNo = appConfig.getPayWechatSerialNo();
        String apiclientKeyPath = appConfig.getPayWechataApiclientKeyPath();
        paramsMap.put("appid", appId);
        paramsMap.put("mchid", mchId);
        paramsMap.put("description", productName);
        paramsMap.put("out_trade_no", orderId);
        //有效期10分钟
        paramsMap.put("time_expire", DateUtil.format(new Date(System.currentTimeMillis() + 1000 * 60 * Constants.ORDER_TIMEOUT_MIN),
                DateTimePatternEnum.YYYY_MM_DDTHH_MM_SS.getPattern()));

        paramsMap.put("notify_url", appConfig.getWebDomain() + String.format(URL_NOTIFY, PayOrderTypeEnum.PAY_WECHAT.getType()));
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", StringTools.convertYuan2fen4BigDecimal(amount));
        amountMap.put("currency", CURRENCY);
        paramsMap.put("amount", amountMap);
        String jsonBody = JsonUtils.convertObj2Json(paramsMap);
        String payUrl = appConfig.getPayDomain() + PAY_URL;
        String token = getToken("POST", payUrl, jsonBody, mchId, serialNo, apiclientKeyPath);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Accept", "application/json");
        if (PayTypeEnum.LAOLUO.getPayType().equals(appConfig.getPayType())) {
            headerMap.put("courseOrderId", appConfig.getCourseOrderId());
        }
        String response = OKHttpUtils.postRequest4Json(payUrl, headerMap, jsonBody);
        Map<String, String> responseMap = JsonUtils.convertJson2Obj(response, Map.class);
        log.info("微信v3接口返回:{},订单Id:{}", response, orderId);
        String codeUrl = responseMap.get("code_url");
        if (StringTools.isEmpty(codeUrl)) {
            throw new BusinessException("获取微信支付信息失败");
        }
        return codeUrl;
    }

    @Override
    public PayOrderNotifyDTO queryOrder(String orderId) {
        String mchId = appConfig.getPayWechatMchid();
        String serialNo = appConfig.getPayWechatSerialNo();
        String apiclientKeyPath = appConfig.getPayWechataApiclientKeyPath();
        String queryUrl = appConfig.getPayDomain() + QUERY_URL;
        String url = String.format(queryUrl, orderId, mchId);
        String token = getToken("GET", url, null, mchId, serialNo, apiclientKeyPath);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Accept", "application/json");
        String response = OKHttpUtils.getRequest(url, headerMap);
        Map<String, Object> bodyInfoMap = JsonUtils.convertJson2Obj(response, Map.class);
        //判断状态
        if (!RETURN_CODE_SUCCESS.equals(bodyInfoMap.get("trade_state"))) {
            return null;
        }
        String channelOrderId = String.valueOf(bodyInfoMap.get("transaction_id"));
        PayOrderNotifyDTO payOrderNotifyDto = new PayOrderNotifyDTO();
        payOrderNotifyDto.setChannelOrderId(channelOrderId);
        payOrderNotifyDto.setOrderId(orderId);
        return payOrderNotifyDto;
    }

    @Override
    public PayOrderNotifyDTO checkPayNotify(Map<String, Object> params, String jsonBody) {
        String wechatpayTimestamp = (String) params.get("wechatpayTimestamp");
        String wechatpayNonce = (String) params.get("wechatpayNonce");
        String wechatpaySignature = (String) params.get("wechatpaySignature");

        String mchNo = appConfig.getPayWechatMchid();
        String serialNo = appConfig.getPayWechatSerialNo();
        String apiclientKeyPath = appConfig.getPayWechataApiclientKeyPath();
        String apiV3Key = appConfig.getPayWechatApiV3Key();

        Map<String, Object> bodyMap = JsonUtils.convertJson2Obj(jsonBody, Map.class);
        Map<String, String> dataMap = (Map) bodyMap.get("resource");
        String associatedData = dataMap.get("associated_data");
        String nonce = dataMap.get("nonce");
        String ciphertext = dataMap.get("ciphertext");
        String bodyInfo = decryptResponseBody(apiV3Key, associatedData, nonce, ciphertext);
        log.info("微信回调V3解密后的结果为:{}", bodyInfo);
        String mySign = wechatpayTimestamp + "\n" + wechatpayNonce + "\n" + jsonBody + "\n";
        Boolean verifyResult = verifySign(mchNo, serialNo, apiclientKeyPath, apiV3Key, wechatpaySignature, mySign);
        log.info("验证结果:{}", verifyResult);
        if (!verifyResult) {
            throw new BusinessException("微信回调V3签名验证失败");
        }
        Map<String, Object> bodyInfoMap = JsonUtils.convertJson2Obj(bodyInfo, Map.class);
        //判断状态
        if (!RETURN_CODE_SUCCESS.equals(bodyInfoMap.get("trade_state"))) {
            throw new BusinessException("微信V3支付回调状态不正确");
        }
        String orderId = String.valueOf(bodyInfoMap.get("out_trade_no"));
        String channelOrderId = String.valueOf(bodyInfoMap.get("transaction_id"));
        PayOrderNotifyDTO payOrderNotifyDto = new PayOrderNotifyDTO();
        payOrderNotifyDto.setChannelOrderId(channelOrderId);
        payOrderNotifyDto.setOrderId(orderId);
        return payOrderNotifyDto;
    }

    private String getToken(String method, String url, String body, String merchantId, String certSerialNo, String keyPath) {
        if (PayTypeEnum.WECHAT.getPayType().equals(appConfig.getPayType())) {
            return getToken4Wechat(method, url, body, merchantId, certSerialNo, keyPath);
        } else if (PayTypeEnum.LAOLUO.getPayType().equals(appConfig.getPayType())) {
            return getToken4Laoluo();
        }
        throw new BusinessException("请配置配置文件中的payType");
    }

    /**
     * 获取微信token
     *
     * @param method
     * @param url
     * @param body
     * @param merchantId
     * @param certSerialNo
     * @param keyPath
     * @return
     * @throws BusinessException
     */
    private String getToken4Wechat(String method, String url, String body, String merchantId, String certSerialNo, String keyPath) throws BusinessException {
        HttpUrl httpurl = HttpUrl.parse(url);
        String nonceStr = StringTools.getRandomString(Constants.LENGTH_30);
        long timestamp = System.currentTimeMillis() / 1000;
        if (StringTools.isEmpty(body)) {
            body = "";
        }
        String message = buildMessage(method, httpurl, timestamp, nonceStr, body);
        String signature = sign(message, keyPath);
        String signStr =
                "mchid=\"" + merchantId + "\",nonce_str=\"" + nonceStr + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + certSerialNo + "\",signature=\"" + signature + "\"";
        log.info("Authorization Token：" + signStr);
        return signStr;
    }

    private String getToken4Laoluo() {
        String courseOrderId = appConfig.getCourseOrderId();
        return StringTools.encodeByMD5(courseOrderId + StringTools.encodeByMD5(courseOrderId));
    }

    private String buildMessage(String method, HttpUrl url, long timestamp, String nonceStr, String body) {
        String canonicalUrl = url.encodedPath();
        if (url.encodedQuery() != null) {
            canonicalUrl += "?" + url.encodedQuery();
        }
        return method + "\n" + canonicalUrl + "\n" + timestamp + "\n" + nonceStr + "\n" + body + "\n";
    }

    private String sign(String str, String keyPath) throws BusinessException {
        try {
            log.info("微信v3参与签名的串为:{}", str.replace("\n", "&"));
            File file = new File(keyPath);
            if (!file.exists()) {
                throw new BusinessException("apiclientKeyPath文件不存在");
            }
            byte[] message = str.getBytes("utf-8");
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(getPrivateKey(keyPath));
            sign.update(message);
            return new String(Base64.getEncoder().encode(sign.sign()));
        } catch (BusinessException e) {
            log.error("生成微信签名失败", e);
            throw e;
        } catch (Exception e) {
            log.error("生成微信签名失败", e);
            throw new BusinessException("生成微信签名失败");
        }
    }


    private PrivateKey getPrivateKey(String filename) throws BusinessException {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)), "utf-8");
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new BusinessException("无效的密钥格式");
        } catch (IOException e) {
            throw new BusinessException("读取秘钥文件失败");
        }
    }

    private Boolean verifySign(String mchId, String serialNo, String keyPath, String apiV3Key, String wechatpaySignature, String mySign) throws BusinessException {
        List<Certificate> certificateList = getCertificate(mchId, serialNo, keyPath, apiV3Key);
        Integer index = 0;
        for (Certificate certificate : certificateList) {
            try {
                index++;
                Signature sign = Signature.getInstance("SHA256withRSA");
                sign.initVerify(certificate);
                sign.update(mySign.getBytes(StandardCharsets.UTF_8));
                Boolean verify = sign.verify(Base64.getDecoder().decode(wechatpaySignature));
                log.info("第{}次验证签名结果{}", index, verify);
                if (verify) {
                    return true;
                }
            } catch (Exception e) {
                log.error("微信回调验证签名失败", e);
            }
        }
        return false;
    }

    private List<Certificate> getCertificate(String mchId, String serialNo, String keyPath, String apiV3Key) throws BusinessException {
        try {
            String certificates_url = appConfig.getPayDomain() + CERTIFICATES_URL;
            String token = getToken("GET", certificates_url, "", mchId, serialNo, keyPath);
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
            headerMap.put("Content-Type", "application/json");
            headerMap.put("Accept", "application/json");
            String body = OKHttpUtils.getRequest(certificates_url, headerMap);
            log.info("获取微信平台证书返回信息:{}}", body);
            Map<String, Object> bodyMap = JsonUtils.convertJson2Obj(body, Map.class);
            List<Map> data = ((List<Map>) bodyMap.get("data"));
            List<Certificate> certificateList = new ArrayList<>();
            for (Map dataMap : data) {
                Map<String, String> encrypt_certificate = (Map<String, String>) dataMap.get("encrypt_certificate");
                String associatedData = encrypt_certificate.get("associated_data");
                String nonce = encrypt_certificate.get("nonce");
                String ciphertext = encrypt_certificate.get("ciphertext");
                String publicKey = decryptResponseBody(apiV3Key, associatedData, nonce, ciphertext);
                final CertificateFactory cf = CertificateFactory.getInstance("X509");
                ByteArrayInputStream inputStream = new ByteArrayInputStream(publicKey.getBytes(StandardCharsets.UTF_8));
                Certificate certificate = cf.generateCertificate(inputStream);
                certificateList.add(certificate);
            }
            return certificateList;
        } catch (BusinessException e) {
            log.error("获取微信平台证书失败", e);
            throw e;
        } catch (Exception e) {
            log.error("获取微信平台证书失败", e);
            throw new BusinessException("获取微信平台证书失败");
        }
    }


    /**
     * 解密信息
     *
     * @param apiV3Key
     * @param associatedData
     * @param nonce
     * @param ciphertext
     * @return
     * @throws BusinessException
     */
    private static String decryptResponseBody(String apiV3Key, String associatedData, String nonce, String ciphertext) throws BusinessException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("微信V3接口解密信息失败，apiV3Key:{},associatedData:{},nonce:{},ciphertext:{}", apiV3Key, associatedData, nonce, ciphertext, e);
            throw new BusinessException("解密失败");
        }
    }

    public static void main(String[] args) {
        String mchId = "1682060827";
        String appId = "wx3d7c9ce879ea7141";
        String certSerialNo = "54CEED93E0574136CD7B55BA3D5AC35353B9F8F2";
        String keyPath = "E:\\webser\\web_app\\easymusic\\key\\apiclient_key.pem";

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", appId);
        paramsMap.put("mchid", mchId);
        paramsMap.put("description", "测试小程序支付");
        paramsMap.put("out_trade_no", StringTools.getRandomString(Constants.LENGTH_30));
        paramsMap.put("time_expire", DateUtil.format(new Date(System.currentTimeMillis() + 1000 * 60 * Constants.ORDER_TIMEOUT_MIN),
                DateTimePatternEnum.YYYY_MM_DDTHH_MM_SS.getPattern()));
        paramsMap.put("notify_url", "http://easyshop.byte361.com/api/payNotify/payNotify4Wechat");
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", StringTools.convertYuan2fen4BigDecimal(new BigDecimal("2.00")));
        amountMap.put("currency", CURRENCY);
        paramsMap.put("amount", amountMap);

        //重点 支付信息
        String openid = "odRm96Mbs5eu7MEwCO1PtO1tL6Io";
        Map<String, Object> payer = new HashMap<>();
        payer.put("openid", openid);
        paramsMap.put("payer", payer);

        String jsonBody = JsonUtils.convertObj2Json(paramsMap);
        String payUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
        String token = new PayChannel4WechatImpl().getToken4Wechat("POST", payUrl, jsonBody, mchId, certSerialNo, keyPath);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Accept", "application/json");
        String response = OKHttpUtils.postRequest4Json(payUrl, headerMap, jsonBody);
        log.info("jsapi支付返回信息:{}", response);
    }
}
