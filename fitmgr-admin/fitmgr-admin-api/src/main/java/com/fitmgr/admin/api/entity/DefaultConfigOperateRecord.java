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
public class DefaultConfigOperateRecord implements Serializable {
    private static final long serialVersionUID = 4052939624635047860L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String uuid;

    private Integer configOperateId;

    private String httpMethod;

    private String httpUrl;

    private String requestBody;

    private String operateUser;

    private LocalDateTime createTime;

    private String status;
}
