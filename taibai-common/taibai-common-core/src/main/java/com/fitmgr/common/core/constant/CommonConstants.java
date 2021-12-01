
package com.taibai.common.core.constant;

/**
 * <p>
 * CommonConstants 通用常量
 * </p>
 *
 * @author Taibai
 * @date 2017/10/29
 */
public interface CommonConstants {
    /**
     * header 中租户ID
     */
    String TENANT_ID = "TENANT-ID";
    /**
     * 删除(禁用)
     */
    String STATUS_DEL = "1";
    /**
     * 正常（启用）
     */
    String STATUS_NORMAL = "0";

    /**
     * 锁定
     */
    String STATUS_LOCK = "9";

    /**
     * 菜单
     */
    String MENU = "0";

    /**
     * 编码
     */
    String UTF8 = "UTF-8";

    /**
     * 前端工程名
     */
    String FRONT_END_PROJECT = "taibai-ui";

    /**
     * 后端工程名
     */
    String BACK_END_PROJECT = "taibai";

    /**
     * 路由存放
     */
    String ROUTE_KEY = "gateway_route_key";

    /**
     * spring boot admin 事件key
     */
    String EVENT_KEY = "event_key";

    /**
     * 验证码前缀
     */
    String DEFAULT_CODE_KEY = "DEFAULT_CODE_KEY_";

    /**
     * 成功标记
     */
    Integer SUCCESS = 0;
    /**
     * 失败标记
     */
    Integer FAIL = 1;

    /**
     * 默认存储bucket
     */
    String BUCKET_NAME = "taibai";

    /**
     * 归属类型：1-租户
     */
    String AFFILIATION_TYPE_TENANT = "0";

    /**
     * 归属类型：2-project
     */
    String AFFILIATION_TYPE_PROJECT = "1";

    Integer MAX_TENANT_LEVEL = 6;

    Integer MAX_SAME_LEVEL_TENANT_COUNT = 1000;
    /**
     * 非全局
     */
    String NOT_GLOBAL = "0";

    /**
     * 全局
     */
    String IS_GLOBAL = "1";
    /**
     * 系统菜单
     */
    String SYSTEM_MENU = "0";

    /**
     * 服务菜单
     */
    String SERVICE_MENU = "1";

    /**
     * 服务模板
     */
    interface TemplateConstants {
        /**
         * 下架
         */
        String STATUS_DOWN = "0";

        /**
         * 上架
         */
        String STATUS_UP = "1";

        /**
         * 英文名前缀
         */
        String PRE_EN_NAME = "Template";

        /**
         * 创建服务模板API
         */
        String CREATE_TEMPLATE_API = "/template/model/create-template";

        /**
         * 创建方式
         */
        interface Create {
            /**
             * 万能表单
             */
            String TYPE_ZERO = "0";

            /**
             * 上传文件
             */
            String TYPE_ONE = "1";

            /**
             * API
             */
            String TYPE_TWO = "2";
        }
    }

    /**
     * 是否弹性伸缩组（是）
     */
    String ELASTICITY_YES = "1";

    /**
     * 是否弹性伸缩组（否）
     */
    String ELASTICITY_NO = "0";

    /**
     * 菜单重定向
     */
    String NO_REDIRECT = "noRedirect";

    /**
     * 租户前缀
     */
    String TENANT_PREFIX = "tenant_";

    /**
     * project前缀
     */
    String PROJECT_PREFIX = "project_";

    /**
     * 用户前缀
     */
    String USER_PREFIX = "user_";

    /**
     * 用户登录token前缀 警告：经核实："1:user_details::" 数字部分并非完全固定
     */
    String USER_LOGIN_PREFIX = "user_details\\:\\:";
    /**
     * 用户默认头像
     */
    String USER_DEFAULT_SETAVATAR = "https://i.loli.net/2020/01/07/7LhjHaqfVdAmQbX.jpg";

    String ENV_SYS_ENV_KEY = "fenghuo_env";

    String ENV_HEADER_ENV_KEY = "fenghuo-env";

    String ENV_TASK_ENV_KEY = "fenghuoEnv";

    String ENV_REGISTER_IP_KEY = "register_ip_address";

    String ENV_EXECUTOR_APP_NAME = "EnvIsoExecAppName";

    /**
     * 录入用户
     */
    String import_user_progress = "import_user_progress_";
    String import_user_fail = "import_user_fail_";
    String import_user_logs = "import_user_logs";

    /**
     * 录入VDC
     */

    String import_vdc_progress = "import_vdc_progress_";
    String import_vdc_fail = "import_vdc_fail_";
    String import_vdc_logs = "import_vdc_logs";

    String LOG_SERVER_ID = "taibai-resourcecenter-biz";

    String LOG_TITLE = "设置存储池性能等级";

    String LOG_BLOCK_STORAGE = "块存储池";
}
