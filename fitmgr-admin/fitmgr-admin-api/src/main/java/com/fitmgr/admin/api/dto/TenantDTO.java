package com.fitmgr.admin.api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fitmgr.admin.api.validation.Save;
import com.fitmgr.admin.api.validation.Update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建人 mhp 创建时间 2019/11/12 描述
 **/

@Data
@EqualsAndHashCode
public class TenantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */

    @ApiModelProperty(value = "id", name = "id", required = false)
    @NotNull(message = ("未指定租户"), groups = { Update.class })
    private Integer id;

    /**
     * 查询参数
     */
    private List<Integer> tenantIds;

    /**
     * 租户描述
     */
    @ApiModelProperty(value = "租户描述", name = "description", required = false)
    @Length(groups = { Save.class, Update.class }, max = 255, message = "描述信息最长为255个字符")
    private String description;

    /**
     * 是否能创建project 0-可以 1-不可以
     */
    @NotBlank(message = "未指定是否可创建project", groups = { Save.class })
    @Pattern(regexp = "[01]", message = "参数[createProject]错误,取值字符0或字符1", groups = { Save.class })
    @ApiModelProperty(value = "能否创建project", name = "createProject", required = true)
    private String createProject;

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
     * 逻辑删：0-正常 1-删除
     */
    @TableLogic
    private String delFlag;

    /**
     * 租户名称
     */
    @NotBlank(message = "租户名称不能为空", groups = { Save.class, Update.class })
    @ApiModelProperty(value = "租户名称", name = "name", required = false)
    private String name;

    /**
     * 租户英文名称
     */
    @NotNull(message = "英文名称不能为空", groups = { Save.class, Update.class })
    @ApiModelProperty(value = "租户英文名称", name = "name", required = false)
    private String englishName;

    /**
     * 租户类型id
     */
    @ApiModelProperty(value = "租户类型id", name = "typeId", required = false)
    private Integer typeId;

    /**
     * 状态 0-启用 1-禁用
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private String status;

    /**
     * 上级id 一级租户默认为0
     */
    @ApiModelProperty(value = "上级id", name = "parentId", required = false)
    private Integer parentId;

    /**
     * 是否限制配额
     */
    @ApiModelProperty(value = "是否限制配额", name = "isLimit", required = false)
    private String isLimit;

    private Integer level;

    private Boolean onlyChild;
}
