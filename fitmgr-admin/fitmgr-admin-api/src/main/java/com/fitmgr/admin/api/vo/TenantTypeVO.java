package com.fitmgr.admin.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建人   mhp
 * 创建时间 2019/12/3
 * 描述
 **/
@Data
public class TenantTypeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Integer id;

    /**
     * 租户类型名称
     */
    private String typeName;

    /**
     * 租户类型描述
     */
    private String description;

    /**
     * 创建时间
     */

    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 配额总量是否限制 0-限制 1-不限制
     */
    private String isLimit;

    /**
     * 逻辑删：0-正常 1-删除
     */
    private String delFlag;
}
