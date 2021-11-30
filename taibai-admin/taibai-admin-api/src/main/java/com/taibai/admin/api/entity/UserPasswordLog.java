package com.taibai.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户密码记录表
 * </p>
 *
 * @author Taibai
 * @since 2020-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "用户密码记录")
public class UserPasswordLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id", name = "userId", required = true)
    private Integer userId;

    /**
     * 密码日志记录（仅保留最近3次）
     */
    @JsonIgnore
    @JsonProperty
    @ApiModelProperty(value = "密码日志纪录", name = "passwordLog", required = true)
    private String passwordLog;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;

}
