package com.taibai.admin.api.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.taibai.admin.api.entity.Auth;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: DZL
 * @Date: 2019/12/9
 * @Description: 角色权限dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleAuthDTO implements Serializable {

    /**
     * 角色id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "角色id", name = "id", required = true)
    private Integer roleId;

    /**
     * 菜单list
     */
    @ApiModelProperty(value = "菜单list", name = "auths", required = true)
    private List<Auth> auths;
}
