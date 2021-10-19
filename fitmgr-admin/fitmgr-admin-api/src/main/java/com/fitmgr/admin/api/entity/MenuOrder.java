package com.fitmgr.admin.api.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MenuOrder implements Serializable {
    private static final long serialVersionUID = 1948324836816292325L;
    @ApiModelProperty(value = "menuId", name = "menuId", required = false)
    private String menuId;

    @ApiModelProperty(value = "menuOrder", name = "menuOrder", required = false)
    private Integer menuOrder;

    @ApiModelProperty(value = "类型", name = "type", required = false)
    private String type;
}
