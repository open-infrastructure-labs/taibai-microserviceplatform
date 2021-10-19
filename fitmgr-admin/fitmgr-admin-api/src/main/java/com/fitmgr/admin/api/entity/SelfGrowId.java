package com.fitmgr.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SelfGrowId implements Serializable {
    private static final long serialVersionUID = 3960957822708247693L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String extend1;
}
