package com.taibai.admin.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 账号锁定策略配置表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AccountLockStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 是否锁定
     */
    @ApiModelProperty(value = "是否锁定", name = "supportLock", required = true)
    private String supportLock;

    /**
     * 最大失败次数
     */
    @ApiModelProperty(value = "最大失败次数", name = "maxFailNum", required = false)
    private Integer maxFailNum;

    /**
     * 锁定时长
     */
    @ApiModelProperty(value = "锁定时长", name = "lockTimeOut", required = false)
    private Integer lockTimeOut;

    /**
     * 解锁类型
     */
    @ApiModelProperty(value = "解锁类型", name = "unlockType", required = false)
    private String unlockType;
}
