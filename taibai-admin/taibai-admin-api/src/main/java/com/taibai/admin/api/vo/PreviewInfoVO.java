package com.taibai.admin.api.vo;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Taibai
 * @date 2020/01/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PreviewInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户数量
     */
    private Integer tenantNumber;

    /**
     * projet数量
     */
    private Integer projectNumber;

    /**
     * 角色数量
     */
    private Integer roleNumber;

    /**
     * 用户数量
     */
    private Integer userNumber;

    private Map<String, Object> recentlyNumber;
}
