package com.fitmgr.common.encrypt.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.encrypt.config.InternalAdminConfig;
import com.fitmgr.common.encrypt.config.KeyYmlModel;
import com.fitmgr.common.encrypt.constant.EncryptConstant;
import com.fitmgr.common.encrypt.constant.UrlConstant;
import com.fitmgr.common.encrypt.enums.SecretKeyType;
import com.fitmgr.common.encrypt.exception.IEncryptInException;
import com.fitmgr.common.encrypt.exception.IEncryptOutException;
import com.fitmgr.common.encrypt.model.EncryptBase64Bean;
import com.fitmgr.common.encrypt.model.KeyVersion;
import com.sun.jersey.core.util.Base64;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class AesUtil {

    private static String ROOT_KEY = "vOdB/6B22vzBU2DKpc92DNmtXFvlndwk5h7bid9P+uE=";
    private static String DEFAULT_KEY_VERSION = "00000001";

    private static String INNER_CALL = "inner";
    private static String HEADER_CALL_MODE = "fitmgr-call-mode";

    private InternalAdminConfig internalAdminConfig;

    private RestTemplate restTemplate;

    public KeyVersion getKeyByKms(String url) {
        HttpEntity<String> entity = TokenUtil.buildGetToken();
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class,
                new HashMap<>());
        return parseResponseEntity(responseEntity);
    }

    public KeyVersion parseResponseEntity(ResponseEntity<Map> responseEntity) {
        KeyVersion keyVersion = new KeyVersion();
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(responseEntity));
        JSONObject bodyJsonObj = jsonObject.getJSONObject("body");
        if (bodyJsonObj != null) {
            Integer code = bodyJsonObj.getInteger("code");
            if (code != null && code.equals(0)) {
                Object dataObj = bodyJsonObj.get("data");
                if (dataObj != null) {
                    JSONObject dataMap = (JSONObject) dataObj;
                    keyVersion.setKey((String) dataMap.get("key"));
                    keyVersion.setVersion((String) dataMap.get("version"));
                    return keyVersion;
                }
            } else {
                throw new RuntimeException("Err Response");
            }
        }
        return keyVersion;
    }

    /**
     * 判断该文本是否被加密过
     * 
     * @param text text
     * @return boolean
     */
    private boolean isEncryptEd(String text) {
        try {
            byte[] cipherBytes = Base64Decoder.decode(text);
            byte[] keyTypeBytes = Arrays.copyOfRange(cipherBytes, 0, EncryptConstant.KEY_TYPE_LEN);
            String secretType = new String(keyTypeBytes, StandardCharsets.UTF_8);
            if (!SecretKeyType.DATA_SECRET.getCode().equals(secretType)) {
                // 业务数据解密后，秘钥类型不是2 就是明文
                return false;
            }
            return !text.equals(decrypt(text));
        } catch (IEncryptOutException iEncryptOutException) {
            throw iEncryptOutException;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 查询活跃秘钥加密敏感数据
     * 
     * @param plaintext
     * @return
     */
    public String encrypt(String plaintext) {
        if (isEncryptEd(plaintext)) {
            log.warn("The text has been encrypted, {}", plaintext);
            return plaintext;
        }
        KeyVersion activeDataKey = KeyMemMgr.getInstance().getActiveDataKey();
        String keyType = SecretKeyType.DATA_SECRET.getCode();
        if (StringUtils.isNotEmpty(activeDataKey.getKey())) {
            return innerEncrypt(plaintext, keyType, activeDataKey.getVersion(), activeDataKey.getKey());
        }
        String url = UrlConstant.URL_ACTIVE_KEYVERSION_BY_KEYTYPE + keyType;
        KeyVersion keyInfo4ui = getKeyByKms(url);
        String secretKeyStr = keyInfo4ui.getKey();
        if (StringUtils.isNotEmpty(secretKeyStr)) {
            KeyMemMgr.getInstance().setActiveDataKey(keyInfo4ui);
        }
        return innerEncrypt(plaintext, keyType, keyInfo4ui.getVersion(), secretKeyStr);
    }

    public String encryptDataKey(String plaintext) {
        // 查询活跃主密钥，在KMS解密好，直接返回明文
        String url = UrlConstant.URL_ACTIVE_KEYVERSION_BY_KEYTYPE + SecretKeyType.MAIN_SECRET.getCode();
        KeyVersion keyInfo4ui = getKeyByKms(url);
        String secretKeyStr = keyInfo4ui.getKey();
        String keyVersion = keyInfo4ui.getVersion();
        String keyType = SecretKeyType.MAIN_SECRET.getCode();
        return innerEncrypt(plaintext, keyType, keyVersion, secretKeyStr);
    }

    /**
     * 为了给config服务的配置文件加密使用的
     * 
     * @param text
     * @return
     */
    public String encryptForCfg(String text) {
        // 查询活跃数据密钥, 更新次数不是很多,没必要查内存
        String url = UrlConstant.URL_ACTIVE_KEYVERSION_BY_KEYTYPE + SecretKeyType.DATA_SECRET.getCode();
        KeyVersion keyInfo4ui = getKeyByKms(url);
        return innerEncrypt(text, SecretKeyType.DATA_SECRET.getCode(), keyInfo4ui.getVersion(), keyInfo4ui.getKey());
    }

    /**
     * 解密，供服务调用接口 适用于数据秘钥，业务数据解密
     * 
     * @param text
     * @return
     */
    public String decrypt(String text) {
        try {
            EncryptBase64Bean encryptBase64Bean = AesUtil.decryptBase64(text);
            String keyVersion = new String(encryptBase64Bean.getKeyVersionBytes(), StandardCharsets.UTF_8);
            String secretType = new String(encryptBase64Bean.getKeyTypeBytes(), StandardCharsets.UTF_8);
            String resultKey = getKeyInMem(keyVersion, secretType);
            if (StringUtils.isNotEmpty(resultKey)) {
                return innerDecrypt(text, resultKey);
            }
            String url = UrlConstant.URL_KEYVERSION_BY_KEYTYPEVERSION + secretType + "/" + keyVersion;
            // 在KMS解密好，直接返回明文
            KeyVersion keyInfo4ui = getKeyByKms(url);
            String secretKeyStr = keyInfo4ui.getKey();
            if (StringUtils.isNotEmpty(secretKeyStr)) {
                KeyMemMgr.getInstance().addKeyInfo(secretType, keyInfo4ui);
            }
            return innerDecrypt(text, secretKeyStr);
        } catch (IEncryptInException ex) {
            log.error("Decrypt failed, return original text, {}", ex.getMessage());
            return text;
        } catch (Exception e) {
            log.error("Decrypt Err, throw exception : {}", e.getMessage());
            throw new IEncryptOutException(e.getMessage());
        }
    }

    /**
     * 获取加密该文本的秘钥和版本
     * 
     * @param text
     * @return
     */
    public KeyVersion getKeyInfo(String text) {
        try {
            EncryptBase64Bean encryptBase64Bean = AesUtil.decryptBase64(text);
            String keyVersion = new String(encryptBase64Bean.getKeyVersionBytes(), StandardCharsets.UTF_8);
            String secretType = new String(encryptBase64Bean.getKeyTypeBytes(), StandardCharsets.UTF_8);
            String resultKey = getKeyInMem(keyVersion, secretType);
            if (StringUtils.isNotEmpty(resultKey)) {
                return new KeyVersion(resultKey, keyVersion);
            }
            String url = UrlConstant.URL_KEYVERSION_BY_KEYTYPEVERSION + secretType + "/" + keyVersion;
            return getKeyByKms(url);
        } catch (Exception ex) {
            log.error("Get key info fail", ex);
            return new KeyVersion();
        }
    }

    public String creatKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256);
            SecretKey secretKey = kg.generateKey();
            return new String(Base64.encode(secretKey.getEncoded()), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm Exception", e);
            throw new IEncryptOutException("Create Key Err");
        }
    }

    /**
     * 通过根秘钥加密
     * 
     * @param text
     * @return
     */
    public String encryptByRootKey(String text) {
        return innerEncrypt(text, SecretKeyType.ROOT_SECRET.getCode(), DEFAULT_KEY_VERSION, ROOT_KEY);
    }

    /**
     * 通过根秘钥解密
     * 
     * @param text
     * @return
     */
    public String decryptByRootKey(String text) {
        return innerDecrypt(text, ROOT_KEY);
    }

    public String innerEncrypt(String plaintext, String keyType, String keyVersion, String secretKeyStr) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(toHash256Deal(secretKeyStr), "AES");
            SecureRandom random = new SecureRandom();
            byte[] ivBytes = new byte[EncryptConstant.IV_LEN];
            random.nextBytes(ivBytes);
            IvParameterSpec parameterSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            int finalEncryptLen = EncryptConstant.KEY_TYPE_LEN + EncryptConstant.KEY_VERSION_LEN
                    + EncryptConstant.IV_LEN + encrypted.length;
            byte[] finalEncrypted = new byte[finalEncryptLen];
            System.arraycopy(keyType.getBytes(StandardCharsets.UTF_8), 0, finalEncrypted, 0,
                    EncryptConstant.KEY_TYPE_LEN);
            System.arraycopy(keyVersion.getBytes(StandardCharsets.UTF_8), 0, finalEncrypted,
                    EncryptConstant.KEY_TYPE_LEN, EncryptConstant.KEY_VERSION_LEN);
            System.arraycopy(ivBytes, 0, finalEncrypted, EncryptConstant.KEY_TYPE_LEN + EncryptConstant.KEY_VERSION_LEN,
                    EncryptConstant.IV_LEN);
            System.arraycopy(encrypted, 0, finalEncrypted,
                    EncryptConstant.KEY_TYPE_LEN + EncryptConstant.KEY_VERSION_LEN + EncryptConstant.IV_LEN,
                    encrypted.length);
            return Base64Encoder.encode(finalEncrypted);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            log.error("Encrypt Err : ", e);
            throw new IEncryptOutException(e.getMessage());
        }
    }

    private EncryptBase64Bean decryptBase64(String cipherText) {
        try {
            byte[] cipherBytes = Base64Decoder.decode(cipherText);
            byte[] keyTypeBytes = Arrays.copyOfRange(cipherBytes, 0, EncryptConstant.KEY_TYPE_LEN);
            byte[] keyVersionBytes = Arrays.copyOfRange(cipherBytes, EncryptConstant.KEY_TYPE_LEN,
                    EncryptConstant.KEY_TYPE_LEN + EncryptConstant.KEY_VERSION_LEN);
            byte[] ivBytes = Arrays.copyOfRange(cipherBytes,
                    EncryptConstant.KEY_TYPE_LEN + EncryptConstant.KEY_VERSION_LEN,
                    EncryptConstant.KEY_TYPE_LEN + EncryptConstant.KEY_VERSION_LEN + EncryptConstant.IV_LEN);
            byte[] dataEncryptBytes = Arrays.copyOfRange(cipherBytes,
                    EncryptConstant.KEY_TYPE_LEN + EncryptConstant.KEY_VERSION_LEN + EncryptConstant.IV_LEN,
                    cipherBytes.length);
            EncryptBase64Bean encryptBase64Bean = new EncryptBase64Bean();
            encryptBase64Bean.setIvBytes(cipherBytes);
            encryptBase64Bean.setKeyTypeBytes(keyTypeBytes);
            encryptBase64Bean.setKeyVersionBytes(keyVersionBytes);
            encryptBase64Bean.setIvBytes(ivBytes);
            encryptBase64Bean.setDataEncryptBytes(dataEncryptBytes);
            return encryptBase64Bean;
        } catch (Exception ex) {
            log.error("decrypt base64 fail ,", ex);
            throw new IEncryptInException("Base64 Err");
        }
    }

    private void generateData(Map<String, String> keyMap) {
        System.out.println("根秘钥明文： " + ROOT_KEY);
        String masterKeyming = "OCbj+GZGbaLPLqkhpAA6zJW033CV0Sn02bRRAjBrsfs=";
        System.out.println("定义为主密钥 ： " + masterKeyming);
        System.out.println("需要存储的加密后主密钥 " + encryptByRootKey(masterKeyming));
        String datakeyming = "JyjrvCUBB4EqA900/Tev/SNwt3yF/h/7mQaEwOOKjuY=";
        System.out.println("定义为数据密钥 ： " + datakeyming);
        log.info("数据秘钥和主密钥的版本为 ： {}", DEFAULT_KEY_VERSION);
        String dataKeymi = innerEncrypt(datakeyming, SecretKeyType.MAIN_SECRET.getCode(), DEFAULT_KEY_VERSION,
                masterKeyming);
        System.out.println("需要存储的加密后数据密钥 " + dataKeymi);
        for (String key : keyMap.keySet()) {
            String pwd = innerEncrypt(key, SecretKeyType.DATA_SECRET.getCode(), DEFAULT_KEY_VERSION, datakeyming);
            log.info("明文key : {} ; 加密后： {}", key, pwd);
        }

        String pig = "MjAwMDAwMDAxEaL8us+3IQP/cfR8FDbRvXHyRbfGrS0yhhYXxxoIbvs=";
        String pigmingwen = innerDecrypt(pig, datakeyming);
        log.info("pig mingwen = {}", pigmingwen);
    }

    private Map<String, String> generateKey() {
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("fhcmp@8!", "");
        keyMap.put("Changeme123", "");
        keyMap.put("admin", "");
        keyMap.put("linksame", "");
        keyMap.put("feifei", "");
        keyMap.put("http://fitmgr:fitmgr@fitmgr-eureka:8761/eureka/", "");
        keyMap.put("gen", "");
        keyMap.put("pig", "");
        keyMap.put("pigx", "");
        keyMap.put("http://fitmgr-config:8888/", "");
        keyMap.put("fitmgr", "");
        keyMap.put("zMFKOwK645Nvup0VYUG4vkdr2zsSTquq", "");
        keyMap.put("123456", "");
        keyMap.put("nginx12!@", "");
        return keyMap;
    }

    public static void main(String[] args) {
        generateData(generateKey());
    }

    /**
     * 配置文件中的密码加解密接口
     * 
     * @param cipherText
     * @param keyYmlModel
     * @return
     */
    public String decryptCfg(String cipherText, KeyYmlModel keyYmlModel) {
        try {
            EncryptBase64Bean encryptBase64DataBean = decryptBase64(cipherText);
            // 解出数据秘钥版本信息 &数据密文
            String keyDataVersion = new String(encryptBase64DataBean.getKeyVersionBytes(), StandardCharsets.UTF_8);
            String keyDataType = new String(encryptBase64DataBean.getKeyTypeBytes(), StandardCharsets.UTF_8);
            // 查询配置文件或者内存得到数据秘钥密文
            String secretDataKeyStr = getKeyByVersion(keyYmlModel.getSetKey(keyDataType), keyDataVersion);

            // 解析得到主密钥版本 主密钥密文
            EncryptBase64Bean encryptBase64Bean = decryptBase64(secretDataKeyStr);
            String keyVersion = new String(encryptBase64Bean.getKeyVersionBytes(), StandardCharsets.UTF_8);
            String keyType = new String(encryptBase64Bean.getKeyTypeBytes(), StandardCharsets.UTF_8);
            String secretKeyStr = getKeyByVersion(keyYmlModel.getSetKey(keyType), keyVersion);

            // 解密得到数据秘钥明文
            String dataKey = decryptAes256(decryptByRootKey(secretKeyStr), encryptBase64Bean.getIvBytes(),
                    encryptBase64Bean.getDataEncryptBytes());
            // 使用数据秘钥解密敏感数据
            return decryptAes256(dataKey, encryptBase64DataBean.getIvBytes(),
                    encryptBase64DataBean.getDataEncryptBytes());

        } catch (IEncryptInException e) {
            log.error("Decrypt failed, return original text, {}", e.getMessage());
            return cipherText;
        } catch (Exception ex) {
            log.error("Decrypt Err, throw exception : {}", ex.getMessage());
            throw new IEncryptOutException(ex.getMessage());
        }
    }

    public String innerDecrypt(String ciphertext, String secretKeyStr) {
        try {
            EncryptBase64Bean encryptBase64Bean = decryptBase64(ciphertext);
            return decryptAes256(secretKeyStr, encryptBase64Bean.getIvBytes(), encryptBase64Bean.getDataEncryptBytes());
        } catch (IEncryptInException e) {
            log.error("Decrypt failed, return original text, {}", e.getMessage());
            return ciphertext;
        } catch (Exception ex) {
            log.error("Decrypt Err, throw exception : {} ", ex.getMessage());
            throw new IEncryptOutException(ex.getMessage());
        }

    }

    private String decryptAes256(String secretKeyStr, byte[] ivBytes, byte[] dataEncryptBytes) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(toHash256Deal(secretKeyStr), "AES");
            IvParameterSpec parameterSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] decrypted = cipher.doFinal(dataEncryptBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            log.error("decrypt aes256 fail, ", ex);
            throw new IEncryptInException(ex.getMessage());
        }
    }

    private String getKeyByVersion(Set<KeyVersion> keyVersions, String version) {
        for (KeyVersion keyVersion : keyVersions) {
            if (keyVersion.getVersion().equals(version)) {
                return keyVersion.getKey();
            }
        }
        return null;
    }

    /**
     * 在内存中查找秘钥,内存中保存明文秘钥
     * 
     * @param keyVersion
     * @param keyType
     * @return
     */
    private String getKeyInMem(String keyVersion, String keyType) {
        KeyMemMgr keyMemMgr = KeyMemMgr.getInstance();
        return keyMemMgr.getKey(keyType, keyVersion);
    }

    /**
     * 将指定字符串做hash算法处理
     * 
     * @param dataStr 需要被处理的字符串
     * @return
     */
    private byte[] toHash256Deal(String dataStr) {
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            digester.update(dataStr.getBytes(StandardCharsets.UTF_8));
            return digester.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IEncryptInException(e.getMessage());
        }
    }

    public String createInternalAdminToken() {
        if (restTemplate == null) {
            restTemplate = SpringContextHolder.getBean("kmsInternalRestTemplate");
        }
        if (internalAdminConfig == null) {
            internalAdminConfig = SpringContextHolder.getBean(InternalAdminConfig.class);
        }
        String url = "http://fitmgr-auth/oauth/token" + "?scope=server&grant_type=password&username="
                + internalAdminConfig.getInternalUserName() + "&password=" + internalAdminConfig.getInternalUserPass();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic dGVzdDp0ZXN0");
        headers.add(HEADER_CALL_MODE, INNER_CALL);
        HttpEntity<Map> entity = new HttpEntity<>(new HashMap<>(), headers);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            log.error("create internal admin token fail ", JSON.toJSONString(responseEntity));
            throw new IEncryptOutException("create internal admin token fail");
        }
        return (String) responseEntity.getBody().get("access_token");
    }
}
