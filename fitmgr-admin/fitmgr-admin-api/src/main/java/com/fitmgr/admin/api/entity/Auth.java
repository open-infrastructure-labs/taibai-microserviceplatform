package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
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
 * 功能权限表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Auth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 角色id
     */
    @NotNull(message = "角色id不能为空")
    @ApiModelProperty(value = "角色id", name = "roleId", required = false)
    private Integer roleId;

    /**
     * 功能id
     */
    @NotNull(message = "功能id不能为空")
    @ApiModelProperty(value = "功能id", name = "functionId", required = false)
    private Integer functionId;

    /**
     * 功能状态：0-启用 1-禁用
     */
    @NotBlank(message = "功能状态不能为空")
    @ApiModelProperty(value = "功能状态", name = "status", required = false)
    private String status = "1";

    /**
     * 功能范围：0-全局，1-租户，2-项目，3-自己, 4-无
     */
    @NotBlank(message = "功能范围不能为空")
    @ApiModelProperty(value = "功能范围", name = "operatingRange", required = false)
    private String operatingRange = "3";

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
     * 逻辑删：0-正常 1-删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除", name = "delFlag", required = false)
    private String delFlag;

    /**
     * VDC级别范围
     */
    @ApiModelProperty(value = "VDC级别范围", name = "tenantRange", required = false)
    private String tenantRange;

}
