package com.fitmgr.common.key.update.receive.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.encrypt.config.ServiceAppNameCfg;
import com.fitmgr.common.encrypt.constant.UrlConstant;
import com.fitmgr.common.encrypt.enums.KeyUpdateStatEnum;
import com.fitmgr.common.encrypt.enums.MqMsgTypeEnum;
import com.fitmgr.common.encrypt.enums.SecretKeyType;
import com.fitmgr.common.encrypt.kms.MqKmsMessage;
import com.fitmgr.common.encrypt.model.KeyVersion;
import com.fitmgr.common.encrypt.util.AesUtil;
import com.fitmgr.common.encrypt.util.KeyMemMgr;
import com.fitmgr.common.encrypt.util.TokenUtil;
import com.fitmgr.common.key.update.receive.ServiceWorker4Kms;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractServiceWorker4Kms implements ServiceWorker4Kms {

    @Autowired
    protected ServiceAppNameCfg serviceAppNameCfg;

    @Autowired
    protected RestTemplate restTemplate;

    public void necessaryWork4DataKey() {
        updateMemKey();
    }

    private void updateMemKey() {
        String url = UrlConstant.URL_ACTIVE_KEYVERSION_BY_KEYTYPE + SecretKeyType.DATA_SECRET.getCode();
        KeyVersion keyInfo4ui = AesUtil.getKeyByKms(url);
        // 在KMS解密好，直接返回明文
        String secretKeyStr = keyInfo4ui.getKey();
        if (StringUtils.isNotEmpty(secretKeyStr)) {
            KeyMemMgr.getInstance().setActiveDataKey(keyInfo4ui);
        }
    }

    public boolean isLegalStatus(String mqMsgType) {
        if (MqMsgTypeEnum.UPDATE_MASTER_KEY.getKey().equals(mqMsgType)) {
            return isLegalStatusMasterKey();
        }
        if (MqMsgTypeEnum.UPDATE_DATA_KEY.getKey().equals(mqMsgType)) {
            return isLegalStatusDataKey();
        }
        return true;
    }

    public boolean updateMasterKey(MqKmsMessage msg) {
        return true;
    }

    private boolean isLegalStatusDataKey() {
        // 判断自己是否在进行更新任务
        if (KeyUpdateStatEnum.UPDATING.getKey().equals(getServiceStatus())) {
            log.warn("Updating");
            return false;
        }
        return true;
    }

    private boolean isLegalStatusMasterKey() {
        // 判断自己是否在进行更新任务
        if (KeyUpdateStatEnum.UPDATING.getKey().equals(getMasterStatus())) {
            log.warn("Updating");
            return false;
        }
        return true;
    }

    public String getMasterStatus() {
        String url = UrlConstant.URL_KEYUPDATESTATUS_BY_KEYTYPE + SecretKeyType.MAIN_SECRET.getCode();
        return parseResponseResult(url);
    }

    private String parseResponseResult(String url) {
        HttpEntity<String> entity = TokenUtil.buildGetToken();
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class,
                new HashMap<>());
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(responseEntity));
        JSONObject bodyJsonObj = jsonObject.getJSONObject("body");
        if (bodyJsonObj != null) {
            Integer code = bodyJsonObj.getInteger("code");
            if (code != null && code.equals(0)) {
                Object dataObj = bodyJsonObj.get("data");
                if (dataObj != null) {
                    return (String) dataObj;
                }
            }
        }
        return "";
    }

    private String getServiceStatus() {
        String appName = serviceAppNameCfg.getApplicationName();
        String url = UrlConstant.URL_SERVICE_UPDATESTATUS_BY_SERVICENAME + appName;
        return parseResponseResult(url);
    }

}
