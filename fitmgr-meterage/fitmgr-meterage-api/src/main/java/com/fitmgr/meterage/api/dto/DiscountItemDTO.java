package com.fitmgr.meterage.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *  折扣项-折扣属性
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DiscountItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String uuid;

    private String discountName;

    private Integer meterageId;

    private String chargeId;

    private BigDecimal currentDiscount;

    private LocalDateTime currentDiscountEffectTime;

    private LocalDateTime planTime;

    private LocalDateTime endTime;

    private Integer discountType;

    private Integer effectRange;

    private Integer tenantId;

    private Integer projectId;

    private Integer discountStatus;

    private String xmlTemplateName;

    private String remark;
}
