package com.taibai.admin.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProjectQuotaAdminVO implements Serializable {
    private static final long serialVersionUID = -5444244649049754148L;
    private Integer userId;
    private String userName;
    private String name;
}
