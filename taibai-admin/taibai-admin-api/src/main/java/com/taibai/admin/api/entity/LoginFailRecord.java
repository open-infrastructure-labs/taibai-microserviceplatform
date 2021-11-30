package com.taibai.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 登录失败记录表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LoginFailRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", name = "userId", required = true)
    private Integer userId;

    /**
     * 用户名/账号
     */
    @ApiModelProperty(value = "用户名", name = "username", required = true)
    private String username;

    /**
     * 失败时间
     */
    @ApiModelProperty(value = "失败时间", name = "failTime", required = false)
    private LocalDateTime failTime;

    /**
     * 是否锁定
     */
    @ApiModelProperty(value = "是否锁定", name = "supportLock", required = true)
    private String supportLock;
}
