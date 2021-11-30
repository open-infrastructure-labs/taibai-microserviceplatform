package com.taibai.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DefaultConfigOperate implements Serializable {
    private static final long serialVersionUID = -8985716941232360554L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String function;

    private String httpMethod;

    private String httpUrl;

    private Boolean setIdFlag;

    private String primaryIdType;

    private String idKeyName;

    private String uuidKeyName;

    private String uuidType;

    private String bodyType;

    private String memberDesc;
}
