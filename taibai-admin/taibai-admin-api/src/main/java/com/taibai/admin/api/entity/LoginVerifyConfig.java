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
public class LoginVerifyConfig implements Serializable {
    private static final long serialVersionUID = -2090650264480052437L;

    @ApiModelProperty(value = "配置id", name = "id", required = false)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "验证方式", name = "verifyType", required = true)
    private String verifyType;

    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", name = "updateTime", required = false)
    private LocalDateTime updateTime;
}
