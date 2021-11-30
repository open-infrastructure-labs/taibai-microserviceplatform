package com.taibai.common.encrypt.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taibai.common.encrypt.enums.SecretKeyType;
import com.taibai.common.encrypt.model.KeyVersion;

import lombok.Getter;
import lombok.Setter;

public class KeyMemMgr {

    private Map<String, String> dataKeyVersionMap = new ConcurrentHashMap<>();
    private Map<String, String> masterKeyVersionMap = new ConcurrentHashMap<>();

    /**
     * 活跃数据秘钥
     */
    @Getter
    @Setter
    private KeyVersion activeDataKey = new KeyVersion();

    private static final KeyMemMgr KEY_MEM_MGR = new KeyMemMgr();

    /**
     * 根据秘钥类型和版本查询内存集合，存在返回对应的秘钥, 不存在返回null
     * 
     * @param keyType
     * @param version
     * @return
     */
    public String getKey(String keyType, String version) {
        if (SecretKeyType.DATA_SECRET.getCode().equals(keyType)) {
            return dataKeyVersionMap.get(version);
        }
        if (SecretKeyType.MAIN_SECRET.getCode().equals(keyType)) {
            return masterKeyVersionMap.get(version);
        }
        return null;
    }

    public static synchronized KeyMemMgr getInstance() {
        if (null == KEY_MEM_MGR) {
            return new KeyMemMgr();
        }
        return KEY_MEM_MGR;
    }

    public void addKeyInfo(String secretKeyType, KeyVersion keyVersion) {
        if (SecretKeyType.MAIN_SECRET.getCode().equals(secretKeyType)) {
            masterKeyVersionMap.put(keyVersion.getVersion(), keyVersion.getKey());
        }
        if (SecretKeyType.DATA_SECRET.getCode().equals(secretKeyType)) {
            dataKeyVersionMap.put(keyVersion.getVersion(), keyVersion.getKey());
        }
    }
}
