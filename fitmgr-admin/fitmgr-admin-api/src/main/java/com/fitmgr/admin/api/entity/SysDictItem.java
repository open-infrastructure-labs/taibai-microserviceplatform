
package com.fitmgr.admin.api.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项
 *
 * @author Fitmgr
 * @date 2019/03/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictItem extends Model<SysDictItem> {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId
    private Integer id;
    /**
     *
     */
    private Integer dictId;
    /**
     * 数据值
     */
    private String value;
    /**
     * 标签名
     */
    private String label;
    /**
     * 类型
     */
    private String type;
    /**
     * 描述
     */
    private String description;
    /**
     * 排序（升序）
     */
    private Integer sort;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 备注信息
     */
    private String remarks;
    /**
     * 删除标记
     */
    @TableLogic
    private String delFlag;
    /**
     * 所属租户
     */
    private Integer tenantId;

}
