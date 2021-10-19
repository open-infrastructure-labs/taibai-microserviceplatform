package com.fitmgr.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建人   mhp
 * 创建时间 2019/11/22
 * 描述
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "用户租户Project关系信息")
public class UserRoleProject implements Serializable {
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
    @NotNull(message = "用户Id不能为空")
    @ApiModelProperty(value = "用户Id", name = "userId", required = true)
    private Integer userId;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID", name = "roleId", required = false)
    private Integer roleId;

    /**
     * 角色ID列表
     */
    @ApiModelProperty(value = "角色ID列表", name = "roleIds", required = false)
    @TableField(exist = false)
    private List<Integer> roleIds;

    /**
     * projectID
     */
    @NotNull(message = "projectId不能为空")
    @ApiModelProperty(value = "projectId", name = "projectId", required = true)
    private Integer projectId;

    private Integer tenantId;
}
