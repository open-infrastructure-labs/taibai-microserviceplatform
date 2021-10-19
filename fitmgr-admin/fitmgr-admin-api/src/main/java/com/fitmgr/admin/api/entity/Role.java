package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fitmgr.admin.api.validation.Save;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色id
     */
    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value = "角色id", name = "id", required = false)
    private Integer id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @ApiModelProperty(value = "角色名称", name = "roleName", required = false)
    private String roleName;

    /**
     * 角色code
     */
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_-]{2,63}$", message = "角色code格式错误", groups = { Save.class })
    @ApiModelProperty(value = "角色code", name = "roleCode", required = false)
    private String roleCode;

    /**
     * 角色描述
     */
    @ApiModelProperty(value = "角色描述", name = "description", required = false)
    private String description;

    /**
     * 父级id
     */
    @NotNull(message = "父级id不能为空")
    @ApiModelProperty(value = "父级id", name = "parentId", required = false)
    private Integer parentId;

    /**
     * 拷贝id
     */
    @ApiModelProperty(value = "拷贝id", name = "inheritId", required = false)
    private Integer inheritId;

    /**
     * 系统角色 0-系统角色 1-自定义角色
     */
    @ApiModelProperty(value = "系统角色 0-系统角色 1-自定义角色", name = "sysRole", required = false)
    private String sysRole;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者", name = "createUser", required = false)
    private String createUser;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间", name = "updateTime", required = false)
    private LocalDateTime updateTime;

    /**
     * 逻辑删：0-正常，1-删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删", name = "delFlag", required = false)
    private String delFlag;

    private Integer level;

    private Boolean tenantDefaultRole;

    private Boolean projectDefaultRole;

    private Boolean systemDefaultRole;
}
