package com.taibai.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserLoginRecord implements Serializable {
    private static final long serialVersionUID = 6556548535154276632L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Long loginCount;
}
