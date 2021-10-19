package com.fitmgr.admin.syncproject.model;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HxBusiInfo implements Serializable {
    private static final long serialVersionUID = 1899189597786312492L;
    @JSONField(name = "tenant_name")
    private String tenantName;

    private String id;
    private String name;
    private String status;
    private String manager;

    @JSONField(name = "manager_op")
    private String managerOp;

    @JSONField(name = "manager_op_b")
    private String managerOpb;

    @JSONField(name = "app_manager")
    private String appManager;

    @JSONField(name = "coss_id")
    private Integer cossId;
}
