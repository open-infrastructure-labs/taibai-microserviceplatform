package com.fitmgr.admin.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 密码规则表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PasswordRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 最小长度
     */
    @ApiModelProperty(value = "最小长度", name = "minLen", required = true)
    private Integer minLen;

    /**
     * 最大长度
     */
    @ApiModelProperty(value = "最大长度", name = "maxLen", required = true)
    private Integer maxLen;

    /**
     * 强度级别
     */
    @ApiModelProperty(value = "强度级别", name = "complexity", required = true)
    private String complexity;
}
