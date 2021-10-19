package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
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
 * 菜单表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 菜单id
     */
    @ApiModelProperty(value = "菜单id", name = "menuId", required = false)
    private String menuId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @ApiModelProperty(value = "菜单名称", name = "name", required = false)
    private String name;

    /**
     * 前端URL
     */
    @ApiModelProperty(value = "前端URL", name = "path", required = false)
    private String path;

    /**
     * 父菜单ID
     */
    @NotNull(message = "父菜单ID不能为空")
    @ApiModelProperty(value = "父菜单ID", name = "parentId", required = false)
    private String parentId;

    /**
     * 菜单类型 菜单类型 0-系统菜单 1-服务菜单
     */
    @ApiModelProperty(value = "菜单类型 菜单类型 0-系统菜单  1-服务菜单", name = "type", required = false)
    private String type;

    /**
     * 标题
     */
    @ApiModelProperty(value = "标题", name = "title", required = false)
    private String title;

    /**
     * 重定向
     */
    @ApiModelProperty(value = "重定向", name = "redirect", required = false)
    private String redirect;

    /**
     * 展示或隐藏（true-展示 false-不展示）默认展示
     */
    @ApiModelProperty(value = "展示或隐藏（true-展示 false-不展示）默认展示", name = "hidden", required = false)
    private Boolean hidden;

    /**
     * 图标
     */
    @ApiModelProperty(value = "图标", name = "icon", required = false)
    private String icon;

    /**
     * VUE页面
     */
    @ApiModelProperty(value = "VUE页面", name = "component", required = false)
    private String component;

    /**
     * 排序值
     */
    @ApiModelProperty(value = "排序值", name = "sort", required = false)
    private Integer sort;

    /**
     * 路由缓冲 0-开启 1-关闭
     */
    @ApiModelProperty(value = "路由缓冲", name = "keepAlive", required = false)
    private String keepAlive;

    /**
     * 状态：0-开启，1- 关闭
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private String status;

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
    @ApiModelProperty(value = "逻辑删", name = "delFlag", required = false)
    private String delFlag;

    /**
     * 顺序
     */
    private Integer menuOrder;

    private Integer isExternal;

    private String externalLoginUrl;

    private String externalUsername;

    private String externalPwd;

    /**
     * 模板ID，若模板为服务模板时有值
     */
    @ApiModelProperty(value = "模板ID", name = "templateId", required = false)
    private Integer templateId;

    /**
     * 是否全租户可用 0 非全局 1 全局
     */
    @ApiModelProperty(name = "isGlobal", value = "是否全租户可用 0 非全局 1 全局")
    private String isGlobal;

}
