package com.taibai.common.core.constant;

/**
 * @author Taibai
 * @date 2019-04-28
 *       <p>
 *       缓存的key 常量
 */
public interface CacheConstants {

    /**
     * 全局缓存，在缓存名称上加上该前缀表示该缓存不区分租户，比如:
     * <p/>
     * {@code @Cacheable(value = CacheConstants.GLOBALLY+CacheConstants.MENU_DETAILS, key = "#roleId  + '_menu'", unless = "#result == null")}
     */
    String GLOBALLY = "gl:";

    /**
     * 验证码前缀
     */
    String DEFAULT_CODE_KEY = "DEFAULT_CODE_KEY:";

    /**
     * 菜单信息缓存
     */
    String MENU_DETAILS = "menu_details";

    /**
     * 用户信息缓存
     */
    String USER_DETAILS = "user_details";

    String USER_DETAILS_PREFIX = "user_details::";

    /**
     * 字典信息缓存
     */
    String DICT_DETAILS = "dict_details";

    /**
     * oauth 客户端信息
     */
    String CLIENT_DETAILS_KEY = SecurityConstants.FITMGR_OAUTH_PREFIX + ":client:details";

    /**
     * spring boot admin 事件key
     */
    String EVENT_KEY = "event_key";

    /**
     * 路由存放
     */
    String ROUTE_KEY = "gateway_route_key";

    /**
     * redis reload 事件
     */
    String ROUTE_REDIS_RELOAD_TOPIC = "gateway_redis_route_reload_topic";

    /**
     * 内存reload 时间
     */
    String ROUTE_JVM_RELOAD_TOPIC = "gateway_jvm_route_reload_topic";

    /**
     * 参数缓存
     */
    String PARAMS_DETAILS = "params_details";

    /**
     * 租户缓存 (不区分租户)
     */
    String TENANT_DETAILS = "tenant_details";
}
