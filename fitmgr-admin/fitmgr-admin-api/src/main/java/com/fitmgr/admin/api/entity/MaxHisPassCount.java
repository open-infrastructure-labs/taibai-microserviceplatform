package com.fitmgr.admin.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 最大历史密码次数表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MaxHisPassCount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色分组id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 角色分组名称
     */
    @ApiModelProperty(value = "最大历史密码次数", name = "count", required = true)
    private Integer count;
}
