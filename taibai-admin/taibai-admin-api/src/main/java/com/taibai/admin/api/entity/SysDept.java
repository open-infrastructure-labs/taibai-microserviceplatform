
package com.taibai.admin.api.entity;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 部门管理
 * </p>
 *
 * @author Taibai
 * @since 2018-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDept extends Model<SysDept> {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    private String name;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    private Integer parentId;

    /**
     * 是否删除 -1：已删除 0：正常
     */
    @TableLogic
    private String delFlag;

}
