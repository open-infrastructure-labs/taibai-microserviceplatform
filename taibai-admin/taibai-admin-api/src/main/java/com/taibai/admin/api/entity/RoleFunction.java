package com.taibai.admin.api.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Taibai
 * @date ：Created in 2021/1/12 17:20
 * @modified By：
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleFunction implements Serializable {

    private static final long serialVersionUID = 1L;

    private String functionName;

    private String roleName;

    private String operatingRange;

}
