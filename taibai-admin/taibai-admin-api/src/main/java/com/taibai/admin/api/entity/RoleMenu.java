package com.taibai.admin.api.entity;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

/**
 * @Auther: DZL
 * @Date: 2019/11/27
 * @Description:
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 角色菜单表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", name = "id", required = false)
    private Integer id;

    /**
     * 角色id
     */
    @NotNull(message = "角色ID不能为空")
    @ApiModelProperty(value = "角色id", name = "roleId", required = false)
    private Integer roleId;

    /**
     * 菜单id
     */
    @NotNull(message = "菜单id不能为空")
    @ApiModelProperty(value = "菜单id", name = "menuId", required = false)
    private String menuId;

    /**
     * 全选半选 true-半选 false-全选
     */
    @NotNull(message = "全选半选 true-半选 false-全选")
    @ApiModelProperty(value = "全选半选 true-半选 false-全选", name = "tinyint", required = false)
    private Boolean half;
}
