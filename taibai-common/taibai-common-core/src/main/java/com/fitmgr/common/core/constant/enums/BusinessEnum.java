package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Classname BusinessEnum
 * @Description 业务枚举
 * @Date 2019/11/11 10:11
 * @Created by DZL
 */
@Getter
@AllArgsConstructor
public enum BusinessEnum {

    /**
     * 参数校验 101-199
     */
    PARAMETER_NULL(101, "参数不能为空"),
    ADD_FAIL(102, "新增失败"),
    UPDATE_FAIL(103, "修改失败"),
    DELETE_FAIL(104, "删除失败"),
    PARAMETER_FAULT(105, "参数异常"),
    FEIGN_FAULT(106, "feign接口异常"),
    FEIGN_CONTEXT(107, "该参数已存在，不可重复提交"),
    PARAMETER_ID_NULL(108, "参数ID不能为空"),

    /**
     * 登录校验 6001-6100
     */
    NOT_LOGIN(6001, "用户未登录"),
    CHECK_PASSWORD(6002, "输入的旧密码不正确"),
    TENANT_FORBIDDEN(6003, "登录所属租户禁用异常"),
    CHECK_USERNAME_RULE(6004, "用户登录账号规则检验异常"),
    CHECK_PASSWORD_RULE(6005, "用户密码规则检验异常"),
    CHECK_PASSWORD_REPETITION(6006, "密码修改禁止与最近3次修改相同"),


    /**
     * 用户中心异常枚举 6101-6200
     */
    USER_REPETITION(6101, "此账户已被注册，请重新输入"),
    USER_TENANT_FORBIDDEN(6102, "所选租户已被禁用，请先获取该租户使用权限"),
    USER_ID_NULL(6103, "用户id为空"),
    USER_TENANT_ID_NULL(6104, "租户id为空"),
    USER_TOKEN_NULL(6106, "当前用户token信息传参异常"),
    USER_FORBID_NAME(6107, "禁止修改当前用户登录用户名"),
    USER_FORBID_MODIFICATION(6108, "禁止修改当前用户密码"),
    USER_NONENTITY(6109, "该用户不存在"),
    USER_PASSWORD_REPETITION(6110, "新密码与原始密码重复,请核实后输入"),
    USER_STATUS_FAULT(6111, "用户状态参数异常:status只能为0或1"),
    USER_ROLE_CODE_FAULT(6112, "角色Code参数异常"),


    /**
     * 文件枚举301- 399
     */
    FILE_NOTFOUND(301, "文件未找到"),

    FILE_FAIL(302, "文件上传失败"),


    /*菜单枚举  1001-1099*/
    MENU_SAVE(1001, "非系统菜单不可新增"),
    MENU_DELET(1002, "非系统菜单不可删除"),
    MENU_UPDATE(1003, "非系统菜单不可修改"),
    SERVICE_MENU_ERROR(1004, "服务菜单获取异常"),
    MENU_CODE_REPEAT(1005, "菜单name已存在！"),
    MENU_NOT_FIND(1006, "找不到该菜单"),
    MENU_NEED_TEMPLATE(1007, "服务菜单需要关联服务模板！"),
    MENU_PATH_REPEAT(1008, "菜单path已存在！"),
    /*功能枚举 1101-1199*/
    FUNCTION_DELET(1101, "该功能占用，无法删除！！！"),
    FUNCTION_CODE(1102, "操作code唯一，code已存在"),

    /*角色枚举 1201-1299*/
    ROLE_DELET(1201, "该角色占用，无法删除"),
    NO_ROLE(1202, "该角色不存在"),
    ROLE_CODE(1203, "英文名称已存在"),
    ROLE_NAME(1204, "角色名称已存在"),
    /*权限枚举 1301-1399*/
    AUTH_NOT(1301, "权限不足"),
    AUTH_CONFIG(1302, "权限配置错误"),


    /*服务模板 2001-2099*/
    MODEL_SAVE(2001, "服务模板新增失败"),

    MODEL_UPDATE(2002, "服务模板修改失败"),

    MODEL_DELETE(2003, "服务模板删除失败"),

    MODEL_STATUS_UP(2004, "服务模板上架失败"),

    MODEL_STATUS_DOWN(2005, "服务模板下架失败"),

    MODEL_MODELCODE_DUPLICATE(2006, "服务模版唯一识别名已存在！"),

    MODEL_WEBURL_DUPLICATE(2007, "数据库存在重复webUrl"),

    MODEL_ROLE_SAVE(2010, "服务模板-角色关联新增失败"),

    MODEL_ROLE_DELETE(2011, "服务模板-角色关联删除失败"),

    MODEL_ROLE_NULL(2012, "服务模板-角色关联集合为空"),

    MODEL_COMPONENT_NULL(2013, "服务模板-组件关联集合为空"),

    MODEL_OLD_ROLE_NULL(2014, "旧角色关联服务模板为空"),

    MODEL_NEW_ROLE_SAVE(2015, "新角色关联服务模板失败"),

    MODEL_CONTAINS_CN(2016, "服务模板识别码不允许包含中文"),

    MODEL_ROLE_NOT_EXIST(2017, "服务模板关联的角色已被删除"),

    MODEL_TENANT_NOT_EXIST(2018, "服务模板关联的租户已被删除"),

    MODEL_TENANT_TYPE_SAVE(2020, "服务模板-租户类型关联新增失败"),

    MODEL_TENANT_TYPE_DELETE(2021, "服务模板-租户类型关联删除失败"),

    MODEL_TENANT_TYPE_NULL(2022, "服务模板-租户类型关联集合为空"),

    MODEL_TENANT_NULL(2022, "服务模板-租户关联集合为空"),

    MODEL_TENANT_SAVE(2020, "服务模板-租户关联新增失败"),

    MODEL_TENANT_DELETE(2021, "服务模板-租户关联删除失败"),

    MODEL_IMAGE_SAVE(2030, "服务模板-镜像新增失败"),

    MODEL_IMAGE_DELETE(2031, "服务模板-镜像关联删除失败"),

    MODEL_IMAGE_NULL(2032, "服务模板-镜像关联集合为空"),

    MODEL_FLAVOR_SAVE(2040, "服务模板-规格新增失败"),

    MODEL_FLAVOR_DELETE(2041, "服务模板-规格关联删除失败"),

    MODEL_FLAVOR_NULL(2042, "服务模板-规格关联集合为空"),

    MODEL_PROVIDER_NULL(2043, "Terrafrom缺少Provider"),

    MODEL_PROVIDER_NOTOBJECT(2044, "Terrafrom的Provider不允许为数组结构"),

    MODEL_RESOURCE_NULL(2045, "Terrafrom缺少Resource"),

    MODEL_RESOURCE_NOTOBJECT(2046, "Terrafrom的Resource不允许为数组结构"),

    MODEL_OUTPUT_NULL(2047, "Terrafrom缺少Output"),

    MODEL_OUTPUT_NOTOBJECT(2048, "Terrafrom的Output不允许为数组结构"),

    MODEL_VARIABLE_NULL(2049, "Terrafrom缺少Variable"),

    MODEL_VARIABLE_NOTOBJECT(2054, "Terrafrom的Variable不允许为数组结构"),

    MODEL_CATEGORY_DEL(2050, "服务模板分类删除失败"),

    MODEL_CATEGORY_OCCUPY(2051, "该分类已关联服务模版不可删除"),

    MODEL_CATEGORY_NULL(2052, "服务模板分类没有数据"),

    MODEL_ENNAME_REPEAT(2053, "英文名不可重复"),

    MODEL_CATEGORY_CONTAINS_CN(2054, "英文名不允许包含中文"),

    MODEL_CATEGORY_NOT_EDIT(2054, "服务模板英文名不允许编辑"),

    DEFINITION_STYLE_LIST_NULL(2060, "UI定义数据集合为空"),

    DEFINITION_STYLE_ID_NULL(2061, "UI定义ID集合为空"),

    DEFINITION_STYLE_BATCH_SAVE(2062, "UI定义批量插入失败"),

    DEFINITION_STYLE_BATCH_DELETE(2063, "UI定义批量删除失败"),

    DEFINITION_COMPONENT_LIST_NULL(2064, "UI定义-组件集合为空"),

    DEFINITION_COMPONENT_BATCH_SAVE(2065, "UI定义-组件批量插入失败"),

    DEFINITION_COMPONENT_BATCH_DELETE(2066, "UI定义-组件批量删除失败"),

    COMPONENT_CODE_EXIT(2067, "该组件已存在,请勿重复添加"),

    COMPONENT_DELETE(2068, "组件-组件属性删除失败"),

    COMPONENT_SAVE(2069, "组件添加失败"),

    COMPONENT_UPDATE(2070, "组件修改失败"),

    COMPONENT_NULL(2071, "未查询到该组件"),

    COMPONENT_TF_NOT_FOUNT(2072, "JSON文件包含的组件无法找到"),

    COMPONENT_CONTAINS_CN(2073, "组件识别码不允许包含中文"),

    OPERAT_CODE_EXIT(2079, "该组件操作已存在,请勿重复添加"),

    OPERAT_NULL(2080, "组件操作不能为空"),

    OPERAT_SAVE(2081, "组件操作新增失败"),

    OPERAT_UPDATE(2082, "组件操作修改失败"),

    OPERAT_DELETE(2083, "组件操作删除失败"),

    LIBRARY_NULL(2084, "组件库没有数据"),

    OPERAT_CODE_NULL(2085, "组件唯一识别码未找到"),

    OPERAT_CONTAINS_CN(2086, "组件操作码不允许包含中文"),

    COMPONENT_BINDER_DELETE(2090, "组件绑定关系删除失败"),

    COMPONENT_DEPEND_RELATIONSHIP_DELETE(2100, "组件依赖关系删除失败"),

    COMPONENT_DEFINITION_DELETE(2110, "组件UI关系删除失败"),

    SERVICE_TEMPLATE_IS_NULL(2111, "服务不存在或已下架！"),

    /**
     * 资源管理枚举  3001-3012
     */
    PRIMARY_KEY_NULL(3000, "主键为空"),

    RESOURCETYPE_ADD_FAIL(3001, "资源类型增加失败"),

    RESOURCETYPE_UPDATE_FAIL(3002, "资源类型修改失败"),

    RESOURCETYPE_DELETE_FAIL(3003, "资源类型删除失败"),

    RESOURCETYPE_RESOURCETYPECODE_EXIT(3004, "资源类型已存在，请勿重复添加"),

    RESOURCEPROVIDER_ADD_FAIL(3005, "provider增加失败"),

    RESOURCEPROVIDER_UPDATE_FAIL(3006, "provider修改失败"),

    RESOURCEPROVIDER_DELETE_FAIL(3007, "provider删除失败"),

    RESOURCEPROVIDER_EXIT(3008, "provider已存在，请勿重复添加"),

    RESOURCEPROVIDERATTRIBUTE_ADD_FAIL(3009, "provider属性增加失败"),

    RESOURCEPROVIDERATTRIBUTE_UPDATE_FAIL(3010, "provider属性修改失败"),

    RESOURCEPROVIDERATTRIBUTE_DELETE_FAIL(3011, "provider属性删除失败"),

    RESOURCEPROVIDERATTRIBUTE_EXIT(3012, "provider属性已存在，请勿重复添加"),

    RESOURCETYPE_NULL(3013, "资源类型查询为空"),

    ATTRIBUTE_IS_NULL(3014, "没有查到对应的属性数据"),

    RESOURCE_OPERATE_PARAMS_REQUIRED(3015, "缺少必填操作参数，请检查"),

    RESOURCE_COMPONENT_NOT_EXIST(3016, "组件不存在，请检查"),

    RESOURCE_OPERATE_NOT_EXIST(3017, "操作不存在，请检查"),

    /**
     * 配额枚举  4001-4099
     */
    QUOTA_RESOURCE_TYPE_FAIL(4001, "资源池类型获取为null"),
    QUOTA_FLAVOR_ID_FAIL(4002, "云主机规格获取为null"),
    INSUFFICIENT_QUOTA(4003, "配额不足"),
    TENANT_DISTRIBUTION_QUOTA(4004, "租户未分配配额"),
    PROJECT_DISTRIBUTION_QUOTA(4005, "project未分配配额"),
    QUOTA_DISTRIBUTION_LARGE(4006, "该配额，大于租户可分配配额"),
    PROJECT_QUOTA_FAIL(4007, "扩容余量，不能大于总量"),
    TENANT_QUOTA_NO(4008, "该租户已分配配额"),
    PROJECT_QUOTA_NO(4009, "该project已分配配额"),
    TENANT_TYPE_GET(4010, "获取租户类型为空"),
    QUOTA_DISTRIBUTION_NULL(4011, "该租户或Project未分配该组件的配额！"),
    QUOTA_APPROVAL(4012, "配额已申请"),
    RESOURCE_CLOUD_LIST(4013, "获取资源管理云列表feign接口异常"),
    RESOURCE_COMPONENT_LIST(4014, "获取资源管理组件列表feign接口异常"),
    RESOURCE_ATTRIBUTE_LIST(4015, "获取资源管理属性列表feign接口异常"),
    TENANT_TYPE_FEIGN(4016, "租户类型feign接口异常"),
    APPROVAL_FEIGN(4017, "配额审批feign接口异常"),
    PENDING_APPROVAL_QUOTA(4018, "没有待审批的配额"),
    PROJECT_VALUE_IS_NULL(4019, "ProjectId为空"),

    /**
     * 消息枚举 5001-5099
     */
    MSG_REPEAT(5001, "配置参数已存在，不可重复提交"),
    MSG_USER_ID(5002, "用户id，不允许为空"),
    MSG_USER_FEIGN(5003, "用户中心feign接口异常"),
    MSG_SYS_FIELD(5004, "系统预置模板，不可删除"),


    /**
     * 回收站枚举 6001-6099
     */

    RECYCLE_COMPLETE_DELETE(6001, "资源管理删除资源feign接口异常"),
    RECYCLE_REVERT(6002, "资源管理还原资源feign接口异常"),
    RECYCLE_SEND_MESSAGE(6003, "资源管理短信通知feign接口异常"),
    RECYCLE_TACTICS_NOT_EXIST(6004, "该资源的回收站策略未创建"),

    /**
     * 对象转换Map ---- 远程接口获取key列表
     */
    REQUEST_PARSE_KEYS(9000, "获取远端数据api解析key失败"),

    /**
     * JSON解析 9100~9500
     */
    JSON_PARSE_EMPTY(9100, "解析出的数据为空"),

    JSON_VALIDATION_FAILED(9101, "Terraform语法校验未通过"),

    JSON_PLAN_FAILED(9102, "JSON文件模板预览未通过"),

    JSON_INTERFACE_FAILED(9103, "调用编排Terraform校验接口异常"),

    JSON_GRAMMAR_FAILED(9104, "不是合法的JSON数据, 请仔细检查是否存在语法错误"),

    JSON_MAX_FAILED(9105, "JSON文件大小超出限制"),

    /**
     * JSON合并 9501~9999
     **/
    JSON_PARSE_BY_KEY(9996, "解析出的key数量大于1"),

    JSON_EMPTY(9997, "JSON数据为空"),

    JSON_WRONG_FORMAT(9998, "JSON格式有误"),

    JSON_WRONG_STRUCTURE(9995, "JSON结构错误,请仔细检查是否存在关键部分缺失"),

    JSON_MERGE(9999, "(TF文件)JSON数据合并失败"),

    /**
     * Activiti 13200 - 13299
     */
    ACTIVITI_MODEL_NULL(13100, "模型图未找到，请添加并保存模型图"),
    ACTIVITI_MODEL_NAME_EXIST(13101, "模型名称或标识已存在，请重新输入"),
    ACTIVITI_MODEL_DEPLOYMENT_EXIST(13102, "该模型存在已部署的流程，不能删除"),
    ACTIVITI_PROCESS_START(13200, "流程实例挂起失败"),
    ACTIVITI_PROCESS_TASK_ALERADY(13201, "该流程实例下存在待办任务"),
    ACTIVITI_PROCESS_NOT_EXISTS(13202, "流程实例不存在，无法进行启用/禁用操作"),
    ACTIVITI_TENANT_MODEL_EXISTS(13203, "该平台工作流下已有关联自定义工作流，不允许多次绑定"),
    ACTIVITI_NOT_AUTH(13300, "该用户无权进行操作"),
    ACTIVITI_SYSTEM_MODEL_CREATE(13301, "非平台级管理员不能创建系统模型，请切换至平台级管理员进行系统模型的创建"),
    ACTIVITI_TENANT_MODEL_CREATE(13302, "非租户管理员不能创建租户自定义模型，请切换至租户管理员进行模型的创建"),
    ACTIVITI_MODEL_KEY_ERROR(13303, "模型标识不能包含有特殊字符 _ "),
    ACTIVITI_SYS_MODEL_DELETE_ERROR(13304, "该模型下有绑定的租户级别模型，不允许删除！"),


    /**
     * 订单14200-14300
     */
    TEMPLATE_NOT_EXIST(14200, "请检查服务模板地址"),
    TEMPLATE_NOT_NULL(14201, "模板参数和执行模板参数不能都为空"),
    TASK_ID_NOT_NULL(14202, "当前任务不存在"),
    ORDER_CRATE_FAILED(14203, "网络异常，订单创建失败，请联系系统管理员进行处理！"),
    ORDER_AUDIT_FAILED(14204, "订单审核失败"),
    APPROVAL_CRATE_FAILED(14205, "订单审核任务创建失败"),
    ORDER_ID_FAILED(14206, "订单编号不存在"),
    ORDER_STATUS_SYNC_FAILED(14207, "订单状态同步失败"),
    TASK_CRATE_FAILED(14208, "订单任务创建失败"),
    TASK_EXECUTE_FAILED(14209, "订单任务执行失败"),
    ORDER_STATUS_EXCEPTION(14210, "订单状态异常"),
    MODULE_SELECT_FAILED(14211, "组件属性查询失败"),
    EXECUTE_TEMPLATE_CRATE_FAILED(14212, "执行模板创建失败"),
    TASK_NODE_CRATE_FAILED(14213, "任务节点构建失败"),
    LAYOUT_CRATE_FAILED(14214, "编排任务创建失败"),
    LAYOUT_EXECUTE_FAILED(14215, "编排任务执行失败，请联系管理员进行处理！"),
    TEMPLATE_NODE_NOT_FOUND(14216, "执行模板中不存在任务节点信息"),
    TEMPLATE_DATA_EXCEPTION(14217, "执行模板数据异常"),
    ENVIEONMENT_RESOURCE_EXCEPTION(14218, "平台管理员不允许创建非环境类资源，请切换到相对应的角色进行资源创建"),
    OPERATOR_ERROR_ORDER(14219, "错误的订单操作类型！"),
    ORDER_TASK_IS_NULL_ERROR(14220, "创建订单事务还未提交，无法更新订单状态！"),
    CREATE_ORDER_TEMPLATE_EXECUTE_PARAM_ERROR(14221, "参数executionTemplate和templateParam不能同时指定"),
    EXPECT_DELIVER_TIME_ERROR(14222, "期望交付日期早于当前日期！"),

    /**
     * 计费 15000-15500
     */
    CHARGE_ITEM_NAME_REPETITION(15000, "新增计费项名称重复，请检查计费项名称！"),
    CHARGE_ITEM_PROPERTY_EMPTY(15001, "计费项的计费属性不能为空！"),
    CHARGE_ITEM_PROPERTY_REPETITION(15002, "该计费项的属性已存在，请修改或重新添加计费项属性！"),
    CHARGE_ITEM_PROPERTY_ENABLE(15003, "计费项处于启用状态或者已被删除，不能进行启用操作！"),
    CHARGE_ITEM_PROPERTY_DISABLE(15005, "计费项处于禁用状态或者已被删除，不能进行禁用操作！"),
    CHARGE_ITEM_ENABLE_OR_DELETE(15006, "计费项处于启用状态或已被删除！"),
    CHARGE_ITEM_IS_NULL(15007, "计费项不存在！"),
    DISCOUNT_ITEM_IS_NULL(15008, "折扣项不存在，无法进行计费匹配！"),
    CHARGE_BILL_REPETITION(15009, "该资源重复计费！"),
    TIME_UNIT_ERROR(15010, "计时单位错误！"),
    CHARGE_ITEM_UUID_NULL(15011, "参数错误，修改计费项，UUID不能为空！"),
    PARAMETER_ERROR(15012, "入参错误！"),
    CHARGEITEM_EXPORT_TEMPLATE_ERROR(15013, "计费项导出模板为NULL"),
    CHARGE_BILL_EXPORT_TEMPLATE_ERROR(15014, "账单记录导出模板为NULL"),
    DISCOUNT_ITEM_EXPORT_TEMPLATE_ERROR(15015, "折扣项导出模板为NULL"),
    UPDATE_ITEM_ERROR(15016, "一个单位计费时间周期内，不允许修改/启用/禁用计费项，否则会造成重复计费"),
    CHARGE_ITEM_PROPERTY_ERROR(15017, "计费项属性重复，请检查！"),
    UPDATE_CHARGE_ITEM_DATE_ERROR(15018, "更新计费项，计划执行时间不能早于当前时间！"),
    UPDATE_CHARGE_ITEM_NAME_REPT(15019, "更新计费项名称重复，请检查计费项名称！"),
    CHARGE_ITEM_NOT_ALLOW_CREATE(15020, "不允许当前操作，该操作创建的计费项与资源已创建的计费项冲突！（一个资源项只被允许创建多个有条件的计费项，或者一个没有条件的超级项，不支持交叉）"),
    UPDATE_CHAEGE_ITEM_EXE_TIME_NULL(15021, "计费项折前单价发生变更，单价修改预计时间不能为空！"),


    /**
     * 计费项
     */
    CREATE_CHARGE_ITEM_ERROR(15100, "新增计费项失败！"),
    UPDATE_CHARGE_ITEM_ERROR(15101, "更新计费项失败！"),
    ENABLE_CHARGE_ITEM_ERROR(15102, "启用计费项失败！"),
    DISABLE_CHARGE_ITEM_ERROR(15103, "禁用计费项失败！"),
    DELETE_CHARGE_ITEM_ERROR(15105, "删除计费项失败！"),
    CREATE_CHARGE_ITEM_PROPERTY_ERROR(15106, "新增计费属性失败！"),
    PLAN_TIME_ERROR(15107, "计划执行时间不能早于当前时间！"),
    CHARGE_RECORDS_CALLBACK_ERROR(15108, "调用计费记录错误，请联系管理员！"),

    /**
     * 折扣项
     */
    CREATE_DISCOUNT_ITEM_ERROR(15200, "新增折扣项失败！"),
    CREATE_DISCOUNT_ITEM_DISABLE(15201, "该类型折扣已经存在，新增折扣项失败! "),


    /**
     * 工单
     */
    CREATE_WORKER_TYPE_NAME_EMPTY(16000, "工单类型名称为空！"),
    CREATE_WORKER_TYPE_ACTIVITI_EMPTY(16001, "工作流不能为空！"),
    CREATE_WORKER_TYPE_ERROR(16002, "新增工单类型失败，请联系管理员进行处理！"),
    UPDATE_WORKER_TYPE_ID_EMPTY(16003, "修改工单，工单ID不能为空！"),
    UPDATE_WORKER_TYPE_IS_EMPTY(16004, "工单类型不存在！"),
    UPDATE_WORKER_TYPE_ERROR(16005, "工单受理失败，请联系管理员进行处理！"),
    UPDATE_WORKER_TYPE_ACTIVITI_ERROR(16006, "未查询到工单与工作流对应关系，更新工单失败！"),
    CREATE_WORKER_TITLE_IS_EMPTY(16007, "工单标题为空！"),
    CREATE_WORKER_DESC_IS_EMPTY(16008, "工单描述为空！"),
    CREATE_WORKER_URGENT_PHONE_EMPTY(16009, "紧急程度为严重或紧急状态手机号码和邮箱不能为空！"),
    CREATE_WORKER_DETAIL_ERROR(16010, "新增工单失败！"),
    CREATE_WORKER_DETAIL_ACTIVITI_EMPTY(16011, "未查询到该工单对应的审批流程，请联系管理员进行处理！"),
    DELETE_WORKER_ERROR(16012, "工单不存在，不允许删除！"),
    DELETE_WORKER_DELETE_ERROR(16013, "工单未撤销或未关闭，不允许删除！"),
    CANCEL_WORKER_ERROR(16014, "工单不存在，不允许撤销！"),
    CANCEL_ACCEPT_WORKER_ERROR(16015, "工单已受理，不允许撤销！"),
    SELECT_WORKER_ID_EMPTY(16016, "工单ID不能为空！"),
    CREATE_WORKER_NODE_ERROR(16017, "新增信息失败，请联系管理员进行处理！"),
    CREATE_WORKER_WORKER_NUM_EMPTY(16018, "新增工单处理信息，工单号不能为空！"),
    WORKER_TYPE_NAME_REP(16019, "工单类型名称重复！"),
    WORKER_EMPTY(16020, "该工单不存在或已被删除，不允许编辑！"),

    ;

    /**
     * 错误编码
     */
    private int code;
    /**
     * 描述
     */
    private String description;

}
