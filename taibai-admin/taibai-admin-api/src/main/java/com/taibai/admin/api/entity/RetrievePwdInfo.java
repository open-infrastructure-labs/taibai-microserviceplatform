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
public class RetrievePwdInfo implements Serializable {
    private static final long serialVersionUID = 739102633206951752L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private LocalDateTime invalidationTime;

    private String hrefSignature;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
