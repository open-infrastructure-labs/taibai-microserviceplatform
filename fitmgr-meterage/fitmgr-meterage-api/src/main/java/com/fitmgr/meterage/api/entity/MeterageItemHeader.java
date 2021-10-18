package com.fitmgr.meterage.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *  计量表头对象
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeterageItemHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id")
    private Integer id;

    /**
     * 计量项id
     */
    @ApiModelProperty(value = "计量项id", name = "meterageItemId")
    private Integer meterageItemId;

    /**
     * 表头key
     */
    @ApiModelProperty(value = "表头key", name = "prop")
    private String prop;

    /**
     * 表头value（中文）
     */
    @ApiModelProperty(value = "表头value", name = "label")
    private String label;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间", name = "updateTime")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除(0=正常,1=删除)
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除(0=正常,1=删除)", name = "delFlag")
    private String delFlag;


}
