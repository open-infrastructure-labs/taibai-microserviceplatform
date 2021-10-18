package com.fitmgr.meterage.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 计费项-入参DTO
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ChargeItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uuid;

    private String chargeName;

    private Integer cloudPlatformId;

    private Integer meterageItemId;

    private String meterageItemName;

    private String componentCode;

    private Integer chargeFlavorUnit;

    private Integer chargeFlavorTime;

    private BigDecimal price;

    private BigDecimal beginPrice;

    private BigDecimal endPrice;

    private Integer chargeStatus;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private LocalDateTime planExecuteTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String chargePropertyKey;

    private String chargePropertyValue;

    private String remark;

    private String xmlTemplateName;

    private Integer current;

    private Integer size;

    private Integer executeFlag;

    private List<ChargeItemPropertyDTO> chargeItemPropertyDTOS;

}
