package com.fitmgr.admin.api.entity;

import java.io.Serializable;

import com.fitmgr.admin.api.sensitive.NoSensitiveObj;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户数量
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserCount implements NoSensitiveObj<UserCount>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * VDC
     */
    private Integer tenantId;

    /**
     * project
     */
    private Integer projectId;

    /**
     * 数量
     */
    private Integer count;

}
