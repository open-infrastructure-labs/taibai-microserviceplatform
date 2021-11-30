
package com.taibai.admin.api.dto;

import java.util.List;

import com.taibai.admin.api.entity.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Taibai
 * @date 2019/11/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends User {

    /**
     * 查询参数
     */
    @ApiModelProperty(value = "查询参数", name = "projectIds", required = true)
    private List<Integer> projectIds;

    /**
     * 查询参数
     */
    @ApiModelProperty(value = "查询参数", name = "tenantIds", required = true)
    private List<Integer> tenantIds;

    /**
     * 查询参数，用户id集合
     */
    @ApiModelProperty(value = "查询参数，用户id集合", name = "userIds", required = true)
    private List<Integer> userIds;

    /**
     * 查询参数
     */
    @ApiModelProperty(value = "查询参数", name = "tenantId", required = true)
    private Integer tenantId;

    /**
     * 查询参数
     */
    @ApiModelProperty(value = "查询参数", name = "projectId", required = true)
    private Integer projectId;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID", name = "role", required = false)
    private List<Integer> role;

    /**
     * 新密码
     */
    @ApiModelProperty(value = "新密码", name = "newpassword1", required = false)
    private String newpassword1;

    /**
     * 文件访问路径
     */
    @ApiModelProperty(value = "文件访问路径", name = "url", required = false)
    private String url;

    /**
     * 当前用户token
     */
    @ApiModelProperty(value = "当前用户token", name = "token", required = false)
    private String token;

    /**
     * 模糊查询名称
     */
    @ApiModelProperty(value = "模糊查询名称", name = "queryName", required = false)
    private String queryName;

}
