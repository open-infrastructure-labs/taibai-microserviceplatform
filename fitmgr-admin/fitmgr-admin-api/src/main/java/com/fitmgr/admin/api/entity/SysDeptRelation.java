
package com.fitmgr.admin.api.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 部门关系表
 * </p>
 *
 * @author Fitmgr
 * @since 2018-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDeptRelation extends Model<SysDeptRelation> {

    private static final long serialVersionUID = 1L;

    /**
     * 祖先节点
     */
    private Integer ancestor;
    /**
     * 后代节点
     */
    private Integer descendant;

}
