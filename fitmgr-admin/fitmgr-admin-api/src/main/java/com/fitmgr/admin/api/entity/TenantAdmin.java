package com.fitmgr.admin.api.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TenantAdmin implements Serializable {
    private static final long serialVersionUID = 7790735446436585248L;

    Integer tenantId;

    List<Integer> userIds;
}
