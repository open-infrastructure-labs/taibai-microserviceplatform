package com.fitmgr.admin.api.entity;

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
public class SessionConfig implements Serializable {
    private static final long serialVersionUID = 1436768165952430094L;

    @ApiModelProperty(value = "配置id", name = "id", required = false)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "是否支持同一账号通过多浏览器同时登录", name = "multiClients", required = false)
    private Boolean multiClients;

    @ApiModelProperty(value = "会话最大时长", name = "sessionMaxValidMinutes", required = false)
    private Integer sessionMaxValidMinutes;

    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", name = "updateTime", required = false)
    private LocalDateTime updateTime;

    private Boolean checkHeartbeat;
}
