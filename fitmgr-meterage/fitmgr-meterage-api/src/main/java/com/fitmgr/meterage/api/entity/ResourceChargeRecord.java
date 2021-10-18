package com.fitmgr.meterage.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 账单明细表
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceChargeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 主键
     */
    private String uuid;

    /**
     * 资源名称
     */
    private String appName;

    /**
     * 实例名称
     */
    private String cmpInstanceName;

    /**
     * 组件CODE
     */
    private String componentCode;

    /**
     * 计量项id
     */
    private Integer meterageId;

    /**
     * 计费项id
     */
    private String chargeId;

    /**
     * 折扣项id
     */
    private String discountId;

    /**
     * 订单号
     */
    private Integer orderNo;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * project id
     */
    private Integer projectId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 创建计费时间：精确到日
     */
    private LocalDateTime chargeBeginTime;

    /**
     * 计费开始时间：精确到秒
     */
    private LocalDateTime beginUseTime;

    /**
     * 计费结束时间：精确到秒
     */
    private LocalDateTime finishUseTime;

    /**
     * 账单周期时间，精确到月，每个月至少产生一个以月为周期单位的账单
     */
    private LocalDateTime billCycleTime;

    /**
     * 用量
     */
    private Integer chargeUsage;

    /**
     * 时长
     */
    private Long duration;

    /**
     * 计费单位
     */
    private String chargeUnit;

    /**
     * 折前单价：元，保留2位小数
     */
    private BigDecimal price;

    /**
     * 折扣：保留两位小数
     */
    private BigDecimal discount;

    /**
     * 总费用：单位元，保留2位小数
     */
    private BigDecimal totalCharge;

    /**
     * 资源数据
     */
    private String resourceData;

    /**
     * 备注
     */
    private String remark;

    /**
     * 0-正常服务
     * 1-服务下线或删除
     */
    private Integer resourceOffFlag;

    /**
     * 0-启用
     * 1-禁用状态
     */
    private Integer enableFlag;

    /**
     * 删除标识：0-展示，1-删除
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
