package com.fitmgr.admin.api.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VDC网络池信息表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NetworkPoolInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String uuid;

    /**
     * 选择1、未选0
     */
    private String choice;

    /**
     * rzId
     */
    private String resourceZoneId;

    public NetworkPoolInfo(String uuid, String choice, String resourceZoneId) {
        super();
        this.uuid = uuid;
        this.choice = choice;
        this.resourceZoneId = resourceZoneId;
    }
}
