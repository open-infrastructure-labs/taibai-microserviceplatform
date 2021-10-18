package com.fitmgr.meterage.api.vo;

import lombok.Data;

import java.io.Serializable;
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
public class ChargeItemPropertyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String uuid;

    private String chargeUuid;

    private String chargePropertyKeyName;

    private String chargePropertyKey;

    private String chargePropertyValue;

    private Integer delFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
