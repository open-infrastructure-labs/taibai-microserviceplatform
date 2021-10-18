package com.fitmgr.meterage.api.dto;

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
public class ResourceChargeRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uuid;

    private String appName;

    private String cmpInstanceName;

    private String componentCode;

    private Integer meterageId;

    private String chargeId;

    private String discountId;

    private Integer orderNo;

    private Integer tenantId;

    private Integer projectId;

    private Integer userId;

    private LocalDateTime chargeBeginTime;

    private LocalDateTime beginUseTime;

    private LocalDateTime finishUseTime;

    private Integer chargeUsage;

    private LocalDateTime billCycleTime;

    private Long duration;

    private String chargeUnit;

    private BigDecimal price;

    private BigDecimal discount;

    private BigDecimal totalCharge;

    private String resourceData;

    private String remark;

    private Integer resourceOffFlag;

    private Integer enableFlag;

    private String keyword;

    private String xmlTemplateName;

    private LocalDateTime tenantTotalMonth;

    private LocalDateTime projectTotalMonth;

    /**
     *  0-半年，1-一年
     */
    private Integer tenantYearStatus;

    /**
     * 0-半年，1-一年
     */
    private Integer projectYearStatus;

    /**
     * 0-租户，1-project
     */
    private Integer searchStatus;

    /**
     * 查询时间
     */
    private LocalDateTime searchMonth;

    private Integer current;

    private Integer size;

}
