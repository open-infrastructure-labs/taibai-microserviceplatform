package com.taibai.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 功能表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Function implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 操作名称
     */
    @NotNull(message = "操作名称不能为空")
    @ApiModelProperty(value = "操作名称", name = "name", required = false)
    private String name;

    /**
     * 菜单id
     */
    @NotNull(message = "菜单id不能为空")
    @ApiModelProperty(value = "菜单id", name = "menuId", required = false)
    private String menuId;

    /**
     * 资源id
     */
    @ApiModelProperty(value = "资源id", name = "resourceId", required = false)
    private Integer resourceId;

    /**
     * 数据范围：0-有 1-无
     */
    @ApiModelProperty(value = "数据范围", name = "dateScope", required = false)
    private String dateScope;

    /**
     * 接口唯一编码
     */
    @ApiModelProperty(value = "接口唯一编码", name = "functionCode", required = false)
    private String functionCode;

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

    private String apiUrl;

    private String httpMethod;
}
