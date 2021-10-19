package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 角色分组角色表
 * 
 * @author Fitmgr
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "角色分组角色信息")
public class RoleGroupRole implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID", name = "roleId", required = false)
    private Integer roleId;

    /**
     * 角色分组ID
     */
    @NotNull(message = "角色分组Id不能为空")
    @ApiModelProperty(value = "角色分组Id", name = "roleGroupId", required = true)
    private Integer roleGroupId;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;
}
