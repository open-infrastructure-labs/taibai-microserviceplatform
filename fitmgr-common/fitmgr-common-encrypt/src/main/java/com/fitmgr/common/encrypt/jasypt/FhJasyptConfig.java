package com.fitmgr.common.encrypt.jasypt;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhJasyptConfig {

    @Bean("fhJasyptEncryptor")
    public StringEncryptor stringEncryptor() {
        return new FhJasyptEncryptor();
    }
}
