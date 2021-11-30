package com.taibai.common.security.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * 
 * 云管鉴权的用户（单点登录专用）
 *
 * @author Taibai
 * @date: 2021年4月9日 下午3:50:16
 */
@Data
public class AuthUser implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4011272745626632669L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色类型
     */
    private Set<String> roleTypes;

    /**
     * 角色codes
     */
    private List<String> roleCodes;

}
