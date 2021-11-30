package com.taibai.admin.api.dto;

import com.taibai.admin.api.entity.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Auther: DZL
 * @Date: 2019/12/9
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ToString
public class TokenDTO implements Serializable {

    /**
     * 当前token
     */
    @ApiModelProperty(value = "当前token", name = "token", required = true)
    private String token;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户信息", name = "user", required = true)
    private User user;

}
