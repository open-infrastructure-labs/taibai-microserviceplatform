package com.taibai.admin.api.entity;

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
 * project表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 项目名称
     */
    @NotNull(message = "项目名称不能为空")
    @ApiModelProperty(value = "项目名称", name = "name", required = true)
    private String name;

    /**
     * 项目描述
     */
    @ApiModelProperty(value = "项目描述", name = "description", required = false)
    private String description;

    /**
     * 租户id
     */
    @NotNull(message = "租户id不能为空")
    @ApiModelProperty(value = "租户id", name = "tenantId", required = true)
    private Integer tenantId;

    /**
     * 状态 0 启用 1禁用
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private String status;

    /**
     * 关联业务 0 -航信 1- 非航信
     */
    private String business;

    /**
     * 是否是默认project 0-是 1-不是
     */
    private String isDefault;

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
     * 外部projectId
     */
    private String exProjectId;

    /**
     * 航信coss_id
     */
    private Integer cossId;
}
