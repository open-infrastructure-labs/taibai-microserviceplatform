package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Classname BusinessEnum
 * @Description API枚举
 * @Date 2019/11/11 10:11
 * @Created by DZL
 */
@Getter
@AllArgsConstructor
public enum ApiEnum {
    /* 角色编码 */
    // 角色查询唯一编码
    ROLE_SELECT("role_select"),
    // 角色删除
    ROLE_DELET("role_delete"),
    // 角色修改
    ROLE_UPDATE("role_update"),

    /* 租户编码 */
    // 添加租户
    ADD_TENANT("add_tenant"),
    // 删除租户
    DELETE_TENANT("delete_tenant"),
    // 修改租户
    UPDATE_TENANT("update_tenant"),
    // 分页查询租户列表
    SELECT_TENANT_LIST("select_tenant_list"),
    // 查询租户类型列表
    SELECT_TENANTTYPE_LIST("select_tenantType_list"),

    /* 租户类型编码 */
    // 添加租户类型
    ADD_TENANTTYPE("add_tenantType"),
    // 删除租户类型
    DELETE_TENANTTYP("delete_tenantTyp"),
    // 修改租户类型
    UPDATE_TENANTTYP("update_tenantTyp"),
    // 查询租户类型
    SELECT_TENANTTYPE("select_tenantType"),

    /* project编码 */
    // 添加project
    ADD_PROJECT("add_project"),
    // 删除project
    DELETE_PROJECT("delete_project"),
    // 修改project
    UPDATE_PROJECT("update_project"),
    // 查询project
    SELECT_PROJECT("select_project"),
    // 查询project列表
    SELECT_PROJECT_LIST("select_project_list"),

    /* user编码 */
    // 添加user
    ADD_USER("add_user"),
    // 删除user
    DELETE_USER("delete_user"),
    // 修改user
    UPDATE_USER("update_user"),
    // 查询user
    SELECT_USER("select_user"),
    // 分页查询用户列表
    SELECT_USER_LIST("select_user_list"),

    // 用户名规则
    USERNAME_RULE("11"),
    // 密码规则(口令至少由8位及以上大小写字母、数字及特殊字符等混合、随机组成（至少包括数字、小写字母、大写字母和特殊符号中的三种";
    PASSWORD_RULE(
            "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_!@#$%^&*`~()-+=]+$)(?![a-z0-9]+$)(?![a-z\\W_!@#$%^&*`~()-+=]+$)(?![0-9\\W_!@#$%^&*`~()-+=]+$)[a-zA-Z0-9\\W_!@#$%^&*`~()-+=]{8,30}$"),

    // 超管
    SUPER_ADMIN("super_admin"),
    // 租户管理员
    TENANT_ADMIN("tenant_admin"),
    // project管理员
    PROJECT_ADMIN("project_admin"),
    // 普通用户
    ORDINARY_USER("ordinary_user"),
    // 租户配额管理员
    TENANT_QUOTA_ADMIN("tenant_quota_admin"),
    // project配额管理员
    PROJECT_QUOTA_ADMIN("project_quota_admin"),
    // project普通用户
    PROJECT_USER("project_user"),

    /* 计量统计 */
    // 分页查询计量记录
    SELECT_METERAGE_RECORD_PAGE("select_meterage_record_page"),
    // 查询计量统计列表
    SELECT_METERAGE_STATISTIC_LIST("select_meterage_statistic_list"),

    /* 日志编码 */
    OPERATE_LOG_SELECT("operate_log_select"),

    /**
     * 权限校验
     */
    AUTH_INSUFFICIENT("权限不足"),

    AUTH("auth");

    /**
     * 唯一编码
     */
    private String code;
}
