package com.taibai.common.encrypt.config;

import com.taibai.common.encrypt.enums.SecretKeyType;
import com.taibai.common.encrypt.model.KeyVersion;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix="kms")
@Component
@Data
public class KeyYmlModel {

    private Set<KeyVersion> masterkey = new HashSet<>();

    private Set<KeyVersion> datakey = new HashSet<>();

    public Set<KeyVersion> getSetKey(String keyType) {
        if (SecretKeyType.DATA_SECRET.getCode().equals(keyType)) {
            return datakey;
        }
        if (SecretKeyType.MAIN_SECRET.getCode().equals(keyType)) {
            return masterkey;
        }
        return null;
    }
}

