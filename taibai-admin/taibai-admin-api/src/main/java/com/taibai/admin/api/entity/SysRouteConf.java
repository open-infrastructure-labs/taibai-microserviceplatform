
package com.taibai.admin.api.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 路由
 *
 * @author Taibai
 * @date 2018-11-06 10:17:18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRouteConf extends Model<SysRouteConf> {
    private static final long serialVersionUID = 1L;
    @JsonIgnore
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 路由ID
     */
    private String routeId;
    /**
     * 路由名称
     */
    private String routeName;
    /**
     * 断言
     */
    private String predicates;
    /**
     * 过滤器
     */
    private String filters;
    /**
     * uri
     */
    private String uri;
    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标识（0-正常,1-删除）
     */
    @TableLogic
    private String delFlag;

}
