package com.taibai.admin.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VDC位置树表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TenantLocationTree implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * VDC id
     */
    @ApiModelProperty(value = "VDC id", name = "vdcId", required = true)
    private Integer vdcId;

    /**
     * 父级VDC id
     */
    @ApiModelProperty(value = "父级VDC id", name = "parentVdcId", required = true)
    private Integer parentVdcId;

    /**
     * 位置树
     */
    @ApiModelProperty(value = "位置树", name = "locationTree", required = true)
    private String locationTree;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号", name = "version", required = false)
    private String treeVersion;
}
