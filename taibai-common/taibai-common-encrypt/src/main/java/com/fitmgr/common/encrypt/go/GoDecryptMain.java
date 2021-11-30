package com.fitmgr.common.encrypt.go;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.fitmgr.common.encrypt.config.KeyYmlModel;
import com.fitmgr.common.encrypt.enums.SecretKeyType;
import com.fitmgr.common.encrypt.model.KeyVersion;
import com.fitmgr.common.encrypt.util.AesUtil;

/**
 * 根据秘钥版本信息 解密数据密文
 */
public class GoDecryptMain {

    public final static DumperOptions DUMPER_OPTIONS = new DumperOptions();
    public final static int ARG_LENGTH = 2;

    static {
        // 设置yaml读取方式为块读取
        DUMPER_OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        DUMPER_OPTIONS.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        DUMPER_OPTIONS.setPrettyFlow(false);
    }

    /**
     * 入参为密文， 和秘钥版本信息存在绝对路径
     * 
     * @param args args
     */
    public static void main(String[] args) {
        if (args.length != ARG_LENGTH) {
            return;
        }
        String secretTextKey = args[0];
        String filePath = args[1];
        decrypt(secretTextKey, filePath);
    }

    private static void decrypt(String secretTextKey, String filePath) {
        if (StringUtils.isEmpty(secretTextKey)) {
            return;
        }
        String result = AesUtil.decryptCfg(secretTextKey, readCfg(filePath));
        System.out.print(result);
    }

    private static KeyYmlModel readCfg(String filePath) {
        FileInputStream fileInputStream = null;
        try {
            Yaml yaml = new Yaml(DUMPER_OPTIONS);
            File file = new File(filePath);
            fileInputStream = new FileInputStream(file);
            Map<String, Object> fileMap = yaml.load(fileInputStream);

            LinkedHashMap kmsMap = (LinkedHashMap) fileMap.get("kms");
            if (kmsMap == null) {
                throw new Exception("There is no kms related configuration in this configuration file");
            }
            List<Map<String, String>> dataMap = (List<Map<String, String>>) kmsMap.get("datakey");
            List<Map<String, String>> mainMap = (List<Map<String, String>>) kmsMap.get("masterkey");
            KeyYmlModel keyYmlModel = new KeyYmlModel();
            buildBean(keyYmlModel, SecretKeyType.MAIN_SECRET.getCode(), mainMap);
            buildBean(keyYmlModel, SecretKeyType.DATA_SECRET.getCode(), dataMap);
            return keyYmlModel;
        } catch (Exception ex) {
            return new KeyYmlModel();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void buildBean(KeyYmlModel keyYmlModel, String keyType, List<Map<String, String>> mapList) {
        Set<KeyVersion> keyVersionSet = keyYmlModel.getSetKey(keyType);
        if (null == keyVersionSet) {
            return;
        }
        for (Map<String, String> stringMap : mapList) {
            keyVersionSet.add(new KeyVersion(stringMap.get("key"), stringMap.get("version")));
        }
    }
}
