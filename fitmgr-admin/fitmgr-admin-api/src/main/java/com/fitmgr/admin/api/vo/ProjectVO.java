package com.fitmgr.admin.api.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

/**
 * 创建人 mhp 创建时间 2019/11/16 描述
 **/
@Data
public class ProjectVO implements Serializable {
    private static final long serialVersionUID = -1336814285269139226L;
    /**
     * id
     */
    private Integer id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 所属租户id
     */
    private Integer tenantId;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 状态 0 启用 1禁用
     */
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
    private LocalDateTime createTime;

    /**
     * 逻辑删 0-正常 1-删除
     */
    private String delFlag;

    /**
     * project配额管理员
     */
    private String pqAdmin;

    /**
     * project配额管理员id
     */
    private Integer pqAdminId;

    @TableField(exist = false)
    private List<ProjectQuotaAdminVO> projectQuotaAdmins;

    /**
     * 所属租户类型名称
     */
    private String typeName;

    /**
     * 配额是否限制 0-限制 1-不限制
     */
    private String isLimit;

    /** projet数量 */
    private Integer projectNumber;

    /**
     * 外部系统的project Id
     */
    private String exProjectId;

    /**
     * 航信coss_id
     */
    private Integer cossId;

}
