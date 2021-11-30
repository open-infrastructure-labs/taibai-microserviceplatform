
package com.taibai.common.core.constant;

/**
 * @author Taibai
 * @date 2017-12-18
 */
public interface SecurityConstants {
    /**
     * 刷新
     */
    String REFRESH_TOKEN = "refresh_token";
    /**
     * 验证码有效期
     */
    int CODE_TIME = 60;
    /**
     * 验证码长度
     */
    String CODE_SIZE = "4";
    /**
     * 角色前缀
     */
    String ROLE = "ROLE_";
    /**
     * 前缀
     */
    String FITMGR_PREFIX = "taibai_";

    /**
     * oauth 相关前缀
     */
    String OAUTH_PREFIX = "oauth:";
    /**
     * 项目的license
     */
    String FITMGR_LICENSE = "made by taibai";

    /**
     * 内部
     */
    String FROM_IN = "Y";

    /**
     * 标志
     */
    String FROM = "from";

    String HEADER_CALL_MODE = "taibai-call-mode";

    String INNER_CALL = "inner";

    /**
     * OAUTH URL
     */
    String OAUTH_TOKEN_URL = "/oauth/token";

    /**
     * 手机号登录URL
     */
    String SMS_TOKEN_URL = "/mobile/token/sms";

    /**
     * 社交登录URL
     */
    String SOCIAL_TOKEN_URL = "/mobile/token/social";
    /**
     * 自定义登录URL
     */
    String MOBILE_TOKEN_URL = "/mobile/token/*";

    String FITMGR_OAUTH_PREFIX = "taibai_oauth";

    /**
     * oauth 客户端信息
     */
    String CLIENT_DETAILS_KEY = FITMGR_OAUTH_PREFIX + ":client:details";

    /**
     * 微信获取OPENID
     */
    String WX_AUTHORIZATION_CODE_URL = "https://api.weixin.qq.com/sns/oauth2/access_token"
            + "?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

    /**
     * {bcrypt} 加密的特征码
     */
    String BCRYPT = "{bcrypt}";
    /**
     * sys_oauth_client_details 表的字段，不包括client_id、client_secret
     */
    String CLIENT_FIELDS = "client_id, CONCAT('{noop}',client_secret) as client_secret, resource_ids, scope, "
            + "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
            + "refresh_token_validity, additional_information, autoapprove";

    /**
     * JdbcClientDetailsService 查询语句
     */
    String BASE_FIND_STATEMENT = "select " + CLIENT_FIELDS + " from sys_oauth_client_details";

    /**
     * 默认的查询语句
     */
    String DEFAULT_FIND_STATEMENT = BASE_FIND_STATEMENT + " order by client_id";

    /**
     * 按条件client_id 查询
     */
    String DEFAULT_SELECT_STATEMENT = BASE_FIND_STATEMENT + " where client_id = ?";

    /**
     * 资源服务器默认bean名称
     */
    String RESOURCE_SERVER_CONFIGURER = "resourceServerConfigurerAdapter";

    /**
     * 客户端模式
     */
    String CLIENT_CREDENTIALS = "client_credentials";

    /**
     * 用户ID字段
     */
    String DETAILS_USER_ID = "user_id";

    /**
     * 用户名字段
     */
    String DETAILS_USERNAME = "username";

    /**
     * 用户部门字段
     */
    String DETAILS_EMAIL = "email";

    /**
     * 租户ID 字段
     */
    String DETAILS_TENANT_ID = "default_tenant_id";

    /**
     * 默认角色字段
     */
//    String DETAILS_DEFAULT_ROLE = "default_role";

    /**
     * 默认project 字段
     */
    String DETAILS_DEFAULT_PROJECT = "default_project";

    /**
     * 协议字段
     */
    String DETAILS_LICENSE = "license";

    /**
     * 激活字段 兼容外围系统接入
     */
    String ACTIVE = "active";

    /**
     * AES 加密
     */
    String AES = "aes";

    String KICKOUT_KEY_PREFIX = FITMGR_OAUTH_PREFIX + ":kickout:token:";

    /**
     * 记录历史密码次数
     */
    Integer HIS_PASS = 6;

}
