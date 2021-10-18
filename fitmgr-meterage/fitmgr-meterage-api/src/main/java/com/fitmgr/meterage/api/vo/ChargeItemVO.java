package com.fitmgr.meterage.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 计费项返回VO
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Data
public class ChargeItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String uuid;

    private String chargeName;

    private Integer cloudPlatformId;

    private String cloudPlatformName;

    private Integer meterageItemId;

    private String meterageItemName;

    private String componentCode;

    private String cloudComponentCode;

    private Integer chargeFlavorUnit;

    private Integer chargeFlavorTime;

    private BigDecimal price;

    private Integer chargeStatus;

    private String chargeStatusName;

    private String remark;

    private Integer delFlag;

    private LocalDateTime createTime;

    private String createTimeStr;

    private LocalDateTime updateTime;

    private LocalDateTime planExecuteTime;

    private Integer executeFlag;

    private String chargeItemPropertyStr;

    private List<ChargeItemPropertyVO> chargeItemPropertyVOS;

}
