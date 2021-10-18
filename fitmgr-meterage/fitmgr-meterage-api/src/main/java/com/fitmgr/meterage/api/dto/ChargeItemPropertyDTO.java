package com.fitmgr.meterage.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 计费项属性-属性DTO
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ChargeItemPropertyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chargeUuid;

    private String chargePropertyKey;

    private String chargePropertyValue;
}
