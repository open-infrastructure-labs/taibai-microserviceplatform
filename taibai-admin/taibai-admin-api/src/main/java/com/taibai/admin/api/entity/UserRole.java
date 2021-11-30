package com.taibai.admin.api.entity;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户角色表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserRole extends Model<UserRole> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", name = "userId", required = true)
    private Integer userId;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @ApiModelProperty(value = "角色ID", name = "roleId", required = true)
    private Integer roleId;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @ApiModelProperty(value = "项目ID", name = "projectId", required = true)
    private Integer projectId;

    private Integer tenantId;
}
