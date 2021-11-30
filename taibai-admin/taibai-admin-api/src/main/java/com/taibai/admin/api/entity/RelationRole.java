package com.taibai.admin.api.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class RelationRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * VDC ID
     */
    private Integer tenantId;
    /**
     * 项目ID
     */
    private Integer projectId;
    /**
     * 角色ID
     */
    private List<Integer> roleIds;
    /**
     * 用户ID
     */
    private List<Integer> userIds;
}
