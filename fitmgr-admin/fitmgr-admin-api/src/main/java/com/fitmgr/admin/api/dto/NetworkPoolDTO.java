
package com.fitmgr.admin.api.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 
 * @author Fitmgr
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NetworkPoolDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -537947496643061070L;

    /**
     * vdcId
     */
    @ApiModelProperty(value = "vdcId", name = "vdcId", required = false)
    private Integer vdcId;

    /**
     * 开1、关0
     */
    @ApiModelProperty(value = "开关", name = "configSwitch", required = false)
    private String configSwitch;

    /**
     * 网络池类型
     */
    @ApiModelProperty(value = "网络池类型", name = "networkPoolType", required = true)
    private String networkPoolType;

    /**
     * 网络池信息
     */
    @ApiModelProperty(value = "网络池信息", name = "networkPoolInfos", required = false)
    private String networkPoolInfos;

    /**
     * 网络池详细信息
     */
    @ApiModelProperty(value = "网络池详细信息", name = "networkPoolDetailInfos", required = false)
    private List<Map<String, Object>> networkPoolDetailInfos;

    /**
     * 同步子集，开1、关0
     */
    @JsonProperty("SyncSubset")
    @ApiModelProperty(value = "同步子集", name = "syncSubset", required = false)
    private Boolean syncSubset;

}
