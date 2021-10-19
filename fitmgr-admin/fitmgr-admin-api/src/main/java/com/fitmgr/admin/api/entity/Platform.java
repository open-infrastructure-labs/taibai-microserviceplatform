package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("platform")
public class Platform implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1796621984244607753L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Long id;

    /**
     * 平台名称
     */
    @ApiModelProperty(value = "归属类型", name = "platformName", required = true)
    private String platformName;

    /**
     * 平台标识
     */
    @ApiModelProperty(value = "平台标识", name = "platformId", required = true)
    private String platformId;

    /**
     * 链接地址（登录成功后跳转）
     */
    @ApiModelProperty(value = "跳转链接", name = "url", required = false)
    private String url;

    /**
     * icon链接
     */
    @ApiModelProperty(value = "icon", name = "icon", required = false)
    private String icon;

    /**
     * 是否单点登录
     */
    @ApiModelProperty(value = "是否单点登录", name = "isSingleLogin", required = true)
    private Integer isSingleLogin;

    /**
     * 协议（0-oauth2, 1-cas）
     */
    @ApiModelProperty(value = "登录协议", name = "protocol", required = true)
    private Integer protocol;

    /**
     * 描述
     */
    @ApiModelProperty(value = "平台描述", name = "description", required = false)
    private String description;

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
