package com.taibai.admin.api.dto;

import com.taibai.admin.api.entity.Session;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SessionDTO extends Session {

    private static final long serialVersionUID = 574940800534899916L;
    @ApiModelProperty(value = "账号", name = "account", required = false)
    private String account;

    @ApiModelProperty(value = "用户名称", name = "userName", required = false)
    private String userName;
}
