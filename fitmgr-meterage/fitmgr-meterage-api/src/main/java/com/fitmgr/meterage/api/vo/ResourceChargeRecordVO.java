package com.fitmgr.meterage.api.vo;

import lombok.Data;

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
public class ResourceChargeRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String uuid;

    private String appName;

    private String cmpInstanceName;

    private String componentCode;

    private Integer meterageId;

    private String meterageName;

    private String chargeId;

    private String chargeName;

    private String discountId;

    private String discountName;

    private Integer orderNo;

    private Integer tenantId;

    private String tenantName;

    private Integer projectId;

    private String projectName;

    private Integer userId;

    private LocalDateTime chargeBeginTime;

    private String chargeBeginTimeStr;

    private LocalDateTime beginUseTime;

    private String beginUseTimeStr;

    private LocalDateTime finishUseTime;

    private String finishUseTimeStr;

    private LocalDateTime billCycleTime;

    private String billCycleTimeStr;

    private Integer chargeUsage;

    private Long duration;

    private String chargeUnit;

    private String chargeUsageString;

    private String durationString;

    private BigDecimal price;

    private String priceStr;

    private BigDecimal discount;

    private String discountStr;

    private BigDecimal totalCharge;

    private String totalChargeStr;

    private String resourceData;

    private String remark;

    private Integer resourceOffFlag;

    private Integer enableFlag;

    private Integer delFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String cycleTime;

    private BigDecimal totalCount;
}
