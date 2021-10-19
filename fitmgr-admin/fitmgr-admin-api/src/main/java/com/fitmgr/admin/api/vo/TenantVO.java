package com.fitmgr.admin.api.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建人 mhp 创建时间 2019/11/12 描述
 **/
@Data
public class TenantVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 租户id
     */
    private Integer id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 英文名称
     */
    private String englishName;
    /**
     * 租户类型id
     */
    private Integer typeId;

    /**
     * 租户类型名
     */
    private String typeName;

    /**
     * 租户描述
     */
    private String description;

    /**
     * 状态 0-启用 1-禁用
     */
    private String status;

    /**
     * 上级id 一级租户默认为0
     */
    private Integer parentId;

    /**
     * 是否能创建project 0-可以 1-不可以
     */
    private String createProject;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删 0-正常 1-删除
     */
    private String delFlag;

    /**
     * 配额总量是否限制 0-限制 1-不限制
     */
    private String isLimit;

    /**
     * 租户数量
     */
    @ApiModelProperty(value = "租户数量", name = "tenantNumber", required = false)
    private Integer tenantNumber;

    private Integer level;
}
