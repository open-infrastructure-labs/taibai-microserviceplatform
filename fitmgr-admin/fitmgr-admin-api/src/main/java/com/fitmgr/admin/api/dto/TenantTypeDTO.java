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

/**
 * 创建人   mhp
 * 创建时间 2019/11/14
 * 描述
 **/
@Data
@EqualsAndHashCode
public class TenantTypeDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = true)
    @NotNull(message = "未指定租户类型",groups = {Update.class})
    private Integer id;

    /**
     * 租户类型名称
     */
    @NotBlank(groups = {Save.class,Update.class},message = "租户类型名称不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5][0-9\\u4e00-\\u9fa5]{4,15}$",message = "租户类型名称只能以中文开头且长度在5-16之间",groups = {Save.class,Update.class})
    @ApiModelProperty(value = "租户类型名称", name = "typeName", required = true)
    private String typeName;

    /**
     * 租户类型描述
     */
    @Length(max = 30,groups = {Save.class,Update.class},message = "描述信息限制30字符以内")
    @ApiModelProperty(value = "租户类型描述", name = "description", required = true)
    private String description;

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

    @NotBlank(message = "未指定是否限制配额总量",groups = {Save.class})
    @ApiModelProperty(value = "限制配额总量", name = "isLimit", required = true)
    private String isLimit;

    /**
     * 逻辑删：0-正常 1-删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除", name = "delFlag", required = false)
    private String delFlag;
}
