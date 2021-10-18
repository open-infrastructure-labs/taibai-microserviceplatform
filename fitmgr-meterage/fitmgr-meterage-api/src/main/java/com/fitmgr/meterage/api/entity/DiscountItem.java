package com.fitmgr.meterage.api.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DiscountItem implements Serializable {

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
     * 折扣项名称
     */
    private String discountName;

    /**
     * 计量项id
     */
    private Integer meterageId;

    /**
     * 计费项id
     */
    private String chargeId;

    /**
     * 当前折扣
     */
    private BigDecimal currentDiscount;

    /**
     * 当前折扣生效时间(精确到秒)
     */
    private LocalDateTime currentDiscountEffectTime;

    /**
     * 计划生效时间（精确到日）
     */
    private LocalDateTime planTime;

    /**
     * 计划失效时间（精确到日）
     */
    private LocalDateTime endTime;

    /**
     * 折扣类型：0-系统，1-手动
     */
    private Integer discountType;

    /**
     * 生效范围：1-全部范围，2-租户范围，3-project范围
     */
    private Integer effectRange;

    /**
     * 租户id：-1：无租户，系统范围；非-1表示具体租户
     */
    private Integer tenantId;

    /**
     * projectid；null：如果tenant_id=-1表示全部范围，tenant_id!=-1,表示租户范围，非null，表示具体的project
     */
    private Integer projectId;

    /**
     * 折扣状态：0-启用 1-禁用
     */
    private Integer discountStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 0-展示，1-删除
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
