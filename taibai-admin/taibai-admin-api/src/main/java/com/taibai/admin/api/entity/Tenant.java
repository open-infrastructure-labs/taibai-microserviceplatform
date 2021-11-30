package com.taibai.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 租户表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-12
 */
@Data
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "租户id", name = "id", required = false)
    private Integer id;

    /**
     * 租户名称
     */
    @ApiModelProperty(value = "租户名称", name = "name", required = true)
    private String name;

    /**
     * 英文名称
     */
    @ApiModelProperty(value = "租户英文名称", name = "englishName", required = false)
    private String englishName;
    /**
     * 租户类型id
     */
    @ApiModelProperty(value = "租户类型id", name = "typeId", required = true)
    private Integer typeId;

    /**
     * 企业描述
     */
    @ApiModelProperty(value = "企业描述", name = "description", required = false)
    private String description;

    /**
     * 状态 0-启用 1-禁用
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private String status;

    /**
     * 上级id 一级租户默认为0
     */

    @ApiModelProperty(value = "上级id", name = "parentId", required = true)
    private Integer parentId;

    /**
     * 是否能创建project 0-可以 1-不可以
     */
    private String createProject;

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

    private Integer level;

}
