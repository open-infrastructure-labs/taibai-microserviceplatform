
package com.taibai.admin.api.dto;

import java.io.Serializable;
import java.util.List;

import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Taibai
 * @date 2017/11/11
 *       <p>
 *       commit('SET_ROLES', data) commit('SET_NAME', data) commit('SET_AVATAR',
 *       data) commit('SET_INTRODUCTION', data) commit('SET_PERMISSIONS', data)
 */
@Data
public class UserInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1938584345886453808L;

    /**
     * 用户基本信息
     */
    @ApiModelProperty(value = "用户基本信息", name = "user", required = false)
    private User user;

    /**
     * VDC集合
     */
    private List<Tenant> tenantList;
}
