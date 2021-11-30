package com.taibai.admin.api.entity;

import lombok.Data;

@Data
public class ProjectOperatingRange {

    /**
     * peojectId
     */
    private Integer projectId;

    /**
     * 用户id
     */
    private Integer userId;

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(!(obj instanceof  ProjectOperatingRange)) {
            return false;
        }

        ProjectOperatingRange u = (ProjectOperatingRange) obj;
        return projectId.equals(u.projectId) && userId.equals(u.userId);
    }

    @Override
    public int hashCode() {
        return projectId.hashCode() + userId.hashCode();
    }
}
