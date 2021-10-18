package com.fitmgr.meterage.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Data
public class DiscountItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String uuid;

    private String discountName;

    private Integer meterageId;

    private String chargeId;

    private String meterageName;

    private BigDecimal currentDiscount;

    private String currentDiscountStr;

    private LocalDateTime currentDiscountEffectTime;

    private String currentDiscountEffectTimeStr;

    private LocalDateTime planTime;

    private String planTimeFormat;

    private LocalDateTime endTime;

    private String endTimeFormat;

    private Integer discountType;

    private String discountTypeName;

    private Integer effectRange;

    private String effectRangeName;

    private Integer tenantId;

    private String tenantName;

    private Integer projectId;

    private String projectName;

    private Integer discountStatus;

    private String discountStatusName;

    private String remark;

    private Integer delFlag;

    private LocalDateTime createTime;

    private String createTimeFormat;

    private LocalDateTime updateTime;
}
