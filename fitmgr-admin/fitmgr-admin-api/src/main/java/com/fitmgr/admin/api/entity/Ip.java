package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("ip_list")
public class Ip implements Serializable {

    private static final long serialVersionUID = 5955343901465560602L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Long id;

    /**
     * 白名单名称
     */
    @ApiModelProperty(value = "白名单名称", name = "name", required = true)
    private String name;

    /**
     * 类型
     */
    @ApiModelProperty(value = "类型", name = "type", required = true)
    private Integer type;

    /**
     * ips
     */
    @ApiModelProperty(value = "ips", name = "ips", required = false)
    private String ips;

    /**
     * cidr
     */
    @ApiModelProperty(value = "cidr", name = "cidr", required = false)
    private String cidr;

    /**
     * IP段起始
     */
    @ApiModelProperty(value = "IP段起始", name = "ipStart", required = false)
    private String ipStart;

    /**
     * IP段结束
     */
    @ApiModelProperty(value = "IP段结束", name = "ipEnd", required = false)
    private String ipEnd;

    /**
     * 状态（0-启用，1-禁用）
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", name = "updateTime", required = false)
    private Date updateTime;

    public Date getCreateTime() {
        Date temp = createTime;
        return temp;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Date getUpdateTime() {
        Date temp = updateTime;
        return temp;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = (Date) updateTime.clone();
    }
}
