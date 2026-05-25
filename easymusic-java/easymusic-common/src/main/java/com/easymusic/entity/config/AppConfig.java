package com.easymusic.entity.config;

import com.easymusic.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.account:admin}")
    private String adminAccount;

    @Value("${admin.password:admin123456}")
    private String adminPassword;

    @Value("${tianpuyue.api.key:}")
    private String tianpuyueApiKey;

    @Value("${tianpuyue.api.domain:https://api.tianpuyue.cn}")
    private String tianpuyueApiDomain;


    @Value("${tianpuyue.api.courseOrderId:}")
    private String tianpuyueApiCourseOrderId;


    @Value("${web.domain:}")
    private String webDomain;


    //微信支付 appid
    @Value("${pay.wechat.appid:}")
    private String payWechatAppId;


    @Value("${pay.wechat.mchid:}")
    private String payWechatMchid;

    //整数
    @Value("${pay.wechat.serialNo:}")
    private String payWechatSerialNo;

    //api密钥
    @Value("${pay.wechat.apiclientKeypath:}")
    private String payWechataApiclientKeyPath;

    // v3key密钥
    @Value("${pay.wechat.apiV3key:}")
    private String payWechatApiV3Key;

    //支付域名
    @Value("${pay.payDomain:}")
    private String payDomain;

    @Value("${pay.payType:wechat}")
    private String payType;

    //课程ID
    @Value("${pay.courseOrderId:}")
    private String courseOrderId;


    @Value("${auto.checkPay:false}")
    private Boolean autoCheckPay;

    @Value("${auto.checkMusic:false}")
    private Boolean autoCheckMusic;


    public String getTianpuyueApiCourseOrderId() {
        return tianpuyueApiCourseOrderId;
    }

    public String getCourseOrderId() {
        return courseOrderId;
    }

    public String getPayDomain() {
        return payDomain;
    }

    public String getPayType() {
        return payType;
    }

    public Boolean getAutoCheckMusic() {
        return autoCheckMusic;
    }

    public Boolean getAutoCheckPay() {
        return autoCheckPay;
    }

    public String getPayWechatApiV3Key() {
        return payWechatApiV3Key;
    }

    public String getPayWechatSerialNo() {
        return payWechatSerialNo;
    }

    public String getPayWechataApiclientKeyPath() {
        return payWechataApiclientKeyPath;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public void setTianpuyueApiKey(String tianpuyueApiKey) {
        this.tianpuyueApiKey = tianpuyueApiKey;
    }

    public void setTianpuyueApiDomain(String tianpuyueApiDomain) {
        this.tianpuyueApiDomain = tianpuyueApiDomain;
    }

    public void setWebDomain(String webDomain) {
        this.webDomain = webDomain;
    }

    public String getPayWechatAppId() {
        return payWechatAppId;
    }

    public void setPayWechatAppId(String payWechatAppId) {
        this.payWechatAppId = payWechatAppId;
    }

    public String getPayWechatMchid() {
        return payWechatMchid;
    }

    public void setPayWechatMchid(String payWechatMchid) {
        this.payWechatMchid = payWechatMchid;
    }

    public String getProjectFolder() {
        if (!StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

    public String getTianpuyueApiDomain() {
        return tianpuyueApiDomain;
    }

    public String getTianpuyueApiKey() {
        return tianpuyueApiKey;
    }

    public String getWebDomain() {
        return webDomain;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
