package com.fitmgr.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Subpage implements Serializable {
    private static final long serialVersionUID = -5929377980524229908L;

    @ApiModelProperty(value = "id", name = "id", required = false)
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @ApiModelProperty(value = "name", name = "name", required = false)
    private String name;

    @ApiModelProperty(value = "routePath", name = "routePath", required = false)
    private String routePath;

    @ApiModelProperty(value = "menuId", name = "menuId", required = false)
    private String menuId;

    @ApiModelProperty(value = "menuPath", name = "menuPath", required = false)
    private String menuPath;

    @ApiModelProperty(value = "parentId", name = "parentId", required = false)
    private Integer parentId;

    @ApiModelProperty(value = "subpage", name = "subpage", required = false)
    private Boolean subpage;
}
