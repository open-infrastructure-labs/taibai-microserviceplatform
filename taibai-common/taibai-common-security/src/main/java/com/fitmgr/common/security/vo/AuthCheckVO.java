package com.taibai.common.security.vo;

import lombok.Data;

/**
 * @Classname AuthCheckVO
 * @Description 权限校验VO
 * @Date 2019/11/19 14:15
 * @Created by DZL
 */
@Data
public class AuthCheckVO {

    /**
     * 唯一编码code
     */
    private String code;

    /**
     * 当前登录用户id
     */
    private Integer userId;

    /**
     * 资源所属租户id
     */
    private Integer tenantId;

    /**
     * 资源所属项目id
     */
    private Integer projectId;

    /**
     * 资源创建者id
     */
    private Integer resourceUserId;
}
