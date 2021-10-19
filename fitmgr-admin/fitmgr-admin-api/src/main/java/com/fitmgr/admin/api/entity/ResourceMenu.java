package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 资源菜单表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", name = "id", required = false)
    private Integer id;

    /**
     * 资源菜单名称
     */
    @NotNull(message = "资源菜单名称不能为空")
    @ApiModelProperty(value = "资源菜单名称", name = "menuName", required = true)
    private String menuName;

    /**
     * 资源菜单编码
     */
    @NotNull(message = "资源菜单名称不能为空")
    @ApiModelProperty(value = "资源菜单编码", name = "menuCode", required = true)
    private String menuCode;

    /**
     * 上级id 一级默认为0
     */
    @NotNull(message = "上级id不能为空")
    @ApiModelProperty(value = "上级id", name = "parentId", required = true)
    private Integer parentId;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间", name = "updateTime", required = false)
    private LocalDateTime updateTime;

    /**
     * 逻辑删：0-正常，1-删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删", name = "delFlag", required = false)
    private String delFlag;

}
