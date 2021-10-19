package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 密码到期表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PassExpiration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id", name = "userId", required = true)
    private Integer userId;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期", name = "date", required = false)
    private LocalDate date;

    /**
     * 发送状态
     */
    @ApiModelProperty(value = "发送状态", name = "sendState", required = false)
    private String sendState;
}
