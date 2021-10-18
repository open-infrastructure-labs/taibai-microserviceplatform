package com.fitmgr.meterage.constant;

import java.math.BigDecimal;

/**
 * @author zhangxiaokang
 * @date 2020/10/23 9:07
 */
public interface ChargeConstant {

    /**
     * 以下常量为创建系统折扣默认值
     * 折扣项名称
     */
    String DISCOUNT_NAME = "系统折扣-";

    /**
     * 默认折扣，不打折：10.00
     */
    BigDecimal SYSTEM_DISCOUNT = BigDecimal.valueOf(10.00);

    /**
     * 0-系统，1-手动
     */
    Integer DISCOUNT_TYPE = 0;

    /**
     * 生效范围：1-全部范围，2-租户范围，3-project范围
     */
    Integer EFFECT_RANGE = 1;

    /**
     * 系统用户，不属于任何租户，tenant_id=1-
     */
    Integer TENANT_ID = -1;

    /**
     * 折扣状态：0-启用 1-禁用
     */
    Integer DISCOUNT_STATUS = 0;

    /**
     * 日期格式
     */
    String DATE_TYPE = "yyyy-MM-dd HH:mm:ss";

    /**
     * 以下常量为计量项属性KEY
     */
    String FLAVOR_ID = "flavor_id";

    /**
     * 以下为变更记录固定备注
     */
    String RESOURCE_OFFINE = "资源下线，计费结算！";

    String CREATE_CHANGE_ITEM = "新增计费项，折前单价为：concurrentPrice ";

    String METERAGE_ITEM_CHANGE = "计费项变更，变更后折前单价为： concurrentPrice ";

    String METERAGE_ITEM_NAME = "计费项名称发生变更，变更后名称为：";

    String METERAGE_ITEM_EXPLANT_TIME = "计费项计划生效时间发生变更，变更后计划生效时间为：";

    String RESOURCE_ITEM_CHANGE = "资源发生变更，停止当前资源计费！";

    String RESOURCE_ITEM_CHANGE_INSERT = "资源发生变更，重新匹配计费信息!";

    String CHARGE_ITEM_CHANGE = "计费项发生变更，当前费用结算，变更后计费单价为： concurrentPrice ";

    String DISABLE_CHARGE_ITEM_CHANGE = "禁用计费项，当前资源使用费用结算!";

    String DELETE_CHARGE_ITEM_CHANGE = "删除计费项，当前资源使用费用结算!";

    String ENABLE_CHARGE_ITEM_CHANGE = "启用计费项，禁用的资源记录重新新增计费记录!";

    String UPDATE_CHARGE_ITEM_CHANGE = "更新计费项，已经结算费用的资源重新新增计费记录!";

    String DISCOUNT_ITEM_CHANGE = "折扣项发生变更，当前费用进行结算，当前折扣价为 concurrentDiscount ，变更之后折扣价格为 changeDiscount ";

    String DISABLE_DISCOUNT_ITEM_CHANGE = "禁用折扣项，当前资源使用费用结算!";

    String DELETE_DISCOUNT_ITEM_CHANGE = "删除折扣项，当前资源使用费用结算!";

    String ENABLE_DISCOUNT_ITEM_CHANGE = "启用折扣项，当前资源使用费用结算!";

    String MONTH_CHARGE_SUM = "月度费用统计，当前统计月份为 month 月份发生的费用!";

    String MONTH_CHARGE_SUM_INSERT_RECORDS = "月度费用结算！";

    String CONCURRENT_PRICE = "concurrentPrice";

    String CHANGE_PRICE = "changePrice";

    String CONCURRENT_DISCOUNT = "concurrentDiscount";

    String CHANGE_DISCOUNT = "changeDiscount";

    String MONTH = "month";

    String UNITARY = "元";

    /**
     * 资源组件类型CODE
     */
    /**
     * 虚拟云主机
     */
    String RESOURCECENTER_COMPUTER = "resourcecenter_compute_instance_v1";

    /**
     * 云硬盘
     */
    String RESOURCECENTER_COMPUTER_BLOCKSTORAGE = "resourcecenter_blockstorage_volume_v1";

    /**
     *  负载均衡
     */
    String RESOURCECNETER_LB_LOADBALANCE = "resourcecenter_lb_loadbalancer_v1";

    /**
     * vpc
     */
    String RESOURCECENTER_NETWORKING_VPC = "resourcecenter_networking_vpc_v1";

    /**
     * 云硬盘属性
     */
    String VOLUMN_SIZE = "volume_size";

    /**
     * 费用统计中文展示字段
     */
    String VIEW_ZH_NAME = "viewZhName";
    String PERCENT = "percent";
    String CHARGE = "charge";
    String DEFAULT_TENANT = "中国民航信息集团";

    String CHARGE_ID = "chargeId";

    String RESOURCE_CHARGE_RECORD_ID = "resourceRecordId";

    String RESOURCE_CHARGE_DATA = "resource_charge_data";

    String INSERT_CHARGE = "新增计费项:";
    String ENABLE_CHARGE = "启用计费项:";
    String UPDATE_CHARGE = "更新计费项:";
    String MONTH_TOTAL = "月度费用统计:";
    String DEFAULT_PERCENT = "0.00%";

    String VALUE = "value";
    String DIC_VALUE = "dicValue";
    String ACCOUNT_CONFIG = "accountConfig";
    String KEY = "key";
    String CHARGE_ITEM_PROPERTIES_UUID = "895b74e7-a87d-4e9e-9d3f-1788aa08d634";
}
