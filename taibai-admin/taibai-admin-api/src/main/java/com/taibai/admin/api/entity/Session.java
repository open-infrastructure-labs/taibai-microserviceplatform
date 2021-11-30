package com.taibai.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Session implements Serializable {
    private static final long serialVersionUID = 178277944234426296L;

    @ApiModelProperty(value = "会话id", name = "id", required = false)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "token", name = "token", required = false)
    private String token;

    @ApiModelProperty(value = "用户ID", name = "userId", required = false)
    private Integer userId;

    @ApiModelProperty(value = "登录时间", name = "loginTime", required = false)
    private LocalDateTime loginTime;

    @ApiModelProperty(value = "客户端IP", name = "ip", required = false)
    private String ip;

    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", name = "updateTime", required = false)
    private LocalDateTime updateTime;

    private Long timeout;
}
