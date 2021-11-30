package com.taibai.admin.api.entity;

import lombok.Data;

@Data
public class ResInfo {

    /**
     * 资源租户ID
     */
    private Integer resTenantId;
    /**
     * 资源项目ID
     */
    private Integer resProjectId;
    /**
     * 资源用户ID
     */
    private Integer resUserId;

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(!(obj instanceof ResInfo)) {
            return false;
        }
        ResInfo u = (ResInfo) obj;
        return resTenantId.equals(u.resTenantId) && resProjectId.equals(u.resProjectId)
                && resUserId.equals(u.resUserId);
    }

    @Override
    public int hashCode() {
        int a = 0;
        if (resTenantId != null) {
            a = a + resTenantId.hashCode();
        }
        if (resProjectId != null) {
            a = a + resProjectId.hashCode();
        }
        if (resUserId != null) {
            a = a + resUserId.hashCode();
        }
        return a;
    }
}
