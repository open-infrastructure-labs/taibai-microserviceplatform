package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 租户类型表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TenantType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 租户类型名称
     */
    @ApiModelProperty(value = "租户类型名称", name = "typeName", required = true)
    private String typeName;

    /**
     * 租户类型描述
     */
    @ApiModelProperty(value = "租户类型描述", name = "description", required = false)
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

    /**
     * 配额总量是否限制 0-限制 1-不限制
     */
    private String isLimit;

    /**
     * 逻辑删：0-正常 1-删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除", name = "delFlag", required = false)
    private String delFlag;
}
