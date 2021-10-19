package com.fitmgr.admin.api.vo;

import java.io.Serializable;

import com.fitmgr.admin.api.entity.Role;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: menghuan
 * @Date: 2020/2/16 9:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleVO extends Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色数量
     */
    private Integer roleNumber;

}
