package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 角色分组表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色分组id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "角色分组id", name = "id", required = false)
    private Integer id;

    /**
     * 角色分组名称
     */
    @NotBlank(message = "角色分组名称不能为空")
    @ApiModelProperty(value = "角色分组名称", name = "name", required = true)
    private String name;

    /**
     * 角色分组描述
     */
    @ApiModelProperty(value = "角色分组描述", name = "description", required = false)
    private String description;

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
}
