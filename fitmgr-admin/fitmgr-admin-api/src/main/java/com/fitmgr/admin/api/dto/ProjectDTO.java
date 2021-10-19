package com.fitmgr.admin.api.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fitmgr.admin.api.validation.Save;
import com.fitmgr.admin.api.validation.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建人   mhp
 * 创建时间 2019/11/15
 * 描述
 **/
@Data
@EqualsAndHashCode
public class ProjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(groups = {Update.class},message = "未指定project")
    @TableId(value = "id",type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 项目名称
     * //    @Pattern(regexp = "^[\\u4e00-\\u9fa5][0-9\\u4e00-\\u9fa5]{4,15}$",message = "项目名称长度在5到16字符之间且只能以中文字开头",groups = {Save.class,Update.class})
     */
    @NotBlank(groups = {Save.class,Update.class},message = "项目名称不能为空")
    @ApiModelProperty(value = "项目名称", name = "name", required = true)
    private String name;

    /**
     * 项目描述
     */
    @Length(max = 255,message = "描述内容255字符以内",groups = {Save.class,Update.class})
    @ApiModelProperty(value = "项目描述", name = "description", required = true)
    private String description;

    /**
     * 租户id
     */
    @NotNull(message = "所属租户不能为空",groups = {Save.class})
    @ApiModelProperty(value = "租户id", name = "tenantId", required = true)
    private Integer tenantId;

    /**
     * 状态 0 启用 1禁用
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private String status;

    /**
     * 关联业务 0 -航信    1- 非航信
     */
    private String business;

    /**
     * 是否是默认project 0-是 1-不是
     */
    private String isDefault;

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
    @ApiModelProperty(value = "逻辑删除", name = "delFlag", required = false)
    private String delFlag;

    /**
     * project配额管理员id
     */
    @ApiModelProperty(value = "project配额管理员id", name = "pqId", required = false)
    private Integer pqAdminId;

    private Integer typeId;

    private String isLimit;

    private Integer userId;

    private List<Integer> tenantIds;

    private List<Integer> projectIds;

    private List<Integer> ids;

}
