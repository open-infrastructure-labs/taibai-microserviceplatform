package com.fitmgr.meterage.api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ChargeItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 计费项单号/主键
     */
    private String uuid;

    /**
     * 计费项名称
     */
    private String chargeName;

    /**
     * 云平台id
     */
    private Integer cloudPlatformId;

    /**
     * 计量项id
     */
    private Integer meterageItemId;

    /**
     * 计量项源组件CODE
     */
    private String componentCode;

    /**
     * 用量单位：取值：/台(默认);/个;/G;/T/Core/partition区/条 等
     */
    private Integer chargeFlavorUnit;

    /**
     * 时间单位：取值：/小时(默认);/天/月/年
     */
    private Integer chargeFlavorTime;

    /**
     * 折前单价: 元，保留2位小数
     */
    private BigDecimal price;

    /**
     * 计费项状态：0-启用 1-禁用
     */
    private Integer chargeStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除：0-正常 1-删除
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 计划执行时间
     */
    private LocalDateTime planExecuteTime;

    /**
     * 计划执行计费项
     */
    private String planExecuteData;

    /**
     * 期望执行标识：0-执行，1-不执行
     */
    private Integer executeFlag;

}
