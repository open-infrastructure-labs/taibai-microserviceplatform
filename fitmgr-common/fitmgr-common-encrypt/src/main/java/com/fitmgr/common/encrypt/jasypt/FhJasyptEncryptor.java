package com.fitmgr.common.encrypt.jasypt;

import com.fitmgr.common.encrypt.util.AesUtil;
import com.fitmgr.common.encrypt.config.KeyYmlModel;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

public class FhJasyptEncryptor implements StringEncryptor {

    @Autowired
    private KeyYmlModel keyYmlModel;

    @Override
    public String encrypt(String message) {
        return AesUtil.encrypt(message);
    }

    @Override
    public String decrypt(String encryptedMessage) {
        return AesUtil.decryptCfg(encryptedMessage, keyYmlModel);
    }

}


