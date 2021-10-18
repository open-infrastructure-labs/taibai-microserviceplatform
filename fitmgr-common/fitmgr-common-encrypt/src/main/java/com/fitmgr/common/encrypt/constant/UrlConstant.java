package com.fitmgr.common.encrypt.constant;

public class UrlConstant {

    private static final String KMS_SERVICE_NAME = "fitmgr-kms-biz";
    private static final String HTTP = "http://";
    private static final String HTTP_PREFIX = HTTP + KMS_SERVICE_NAME;

    /**
     * 通过秘钥类型查询活跃秘钥
     */
    public static final String URL_ACTIVE_KEYVERSION_BY_KEYTYPE = HTTP_PREFIX + "/key/active/by-key-type/";

    /**
     * 通过秘钥类型 查询活跃密文秘钥信息
     */
    public static final String URL_ACTIVE_SECRET_KEYVERSION_BY_KEYTYPE = HTTP_PREFIX
            + "/key/active/secret/by-key-type/";

    /**
     * 通过秘钥类型和版本 查询秘钥信息
     */
    public static final String URL_KEYVERSION_BY_KEYTYPEVERSION = HTTP_PREFIX + "/key/by-type-version/";

    /**
     * 查询系统秘钥更新的状态（主密钥或数据秘钥）
     */
    public static final String URL_KEYUPDATESTATUS_BY_KEYTYPE = HTTP_PREFIX + "/key/status/by-type/";

    /**
     * 通过服务名查询服务的秘钥更新状态
     */
    public static final String URL_SERVICE_UPDATESTATUS_BY_SERVICENAME = HTTP_PREFIX
            + "/key/service-status/by-service/";

    /**
     * 服务注册
     */
    public static final String URL_SERVICE_REGISTER_TO_KMS = HTTP_PREFIX + "/notify/service/";

    /**
     * 服务通知kms秘钥更新结果
     */
    public static final String URL_SERVICE_NOTIFY_TO_KMS_KEYUPDATESTATUS = HTTP_PREFIX + "/notify/ack/service/";

    /**
     * 服务通知主密钥更新结果
     */
    public static final String URL_SERVICE_NOTIFY_TO_KMS_MASTERKEYUPDATESTATUS = HTTP_PREFIX + "/notify/ack/master/";

}
