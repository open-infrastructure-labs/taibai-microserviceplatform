package com.taibai.common.encrypt.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class KeyVersion implements Serializable {

    private static final long serialVersionUID = -3627798091071231095L;

    public KeyVersion() {

    }

    public KeyVersion(String key, String version) {
        this.key = key;
        this.version = version;
    }

    private String key;
    private String version;
}
