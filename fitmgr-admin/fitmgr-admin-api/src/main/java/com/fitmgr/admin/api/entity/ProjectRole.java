package com.fitmgr.admin.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectRole extends Project {

    private static final long serialVersionUID = 8634759679472406665L;

    private Integer roleId;

    private String roleName;
}
