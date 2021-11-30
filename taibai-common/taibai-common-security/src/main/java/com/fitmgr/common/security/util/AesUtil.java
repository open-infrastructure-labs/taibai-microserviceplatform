package com.fitmgr.common.security.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AesUtil {
    private static final String KEY_ALGORITHM = "AES";

    public static String decryptAes(String data, String pass) {
        try {
            AES aes = new AES(Mode.CBC, Padding.NoPadding,
                    new SecretKeySpec(pass.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM),
                    new IvParameterSpec(pass.getBytes(StandardCharsets.UTF_8)));
            byte[] result = aes.decrypt(Base64.decode(data.trim().getBytes(StandardCharsets.UTF_8)));
            return new String(result, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            log.error("AES decrypt fail, data={}", data, e);
            return data;
        }
    }

}
