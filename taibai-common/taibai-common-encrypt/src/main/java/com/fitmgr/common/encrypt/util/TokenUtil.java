package com.fitmgr.common.encrypt.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TokenUtil {

    private static String BEARER = "Bearer ";

    public static HttpEntity<Map> buildToken() {
        HttpHeaders headers = new HttpHeaders();
        String token = AesUtil.createInternalAdminToken();
        headers.add("Authorization", TokenUtil.BEARER + token);
        return new HttpEntity<Map>(new HashMap<>(), headers);
    }

    public static HttpEntity<String> buildGetToken() {
        HttpHeaders requestHeaders = new HttpHeaders();
        String token = AesUtil.createInternalAdminToken();
        requestHeaders.set("Authorization", TokenUtil.BEARER + token);
        return new HttpEntity<String>(requestHeaders);
    }

}
