package com.fitmgr.admin.api.vo;

import com.fitmgr.admin.api.entity.Auth;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname AuthVO
 * @Description 菜单操作 VO
 * @Date 2019/11/14 16:54
 * @Created by DZL
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AuthVO extends Auth implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 功能名称
     */
    private String name;

    /**
     * 功能code
     */
    private String functionCode;

    /**
     * 角色code
     */
    private String roleCode;

    /**
     * 当前用户 所属租户id
     */
    private Integer tenantId;

    /**
     * 默认project
     */
    private Integer defaultProject;

    /**
     * 当前用户的用户id
     */
    private Integer userId;

    /**
     * 资源id
     */
    private Integer resourceId;

    /**
     * 功能权限 0-有 1-无
     */
    private Integer operationAuth;

    /**
     * 功能范围：0-全局，1-租户，2-项目，3-自己, 4-无
     */
    private String operatingRange;


    /**
     * 数据范围：0-有 1-无
     */
    private String dateScope;

}
