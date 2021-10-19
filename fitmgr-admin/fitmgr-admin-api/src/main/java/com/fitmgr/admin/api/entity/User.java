package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitmgr.admin.api.sensitive.NoSensitiveObj;
import com.fitmgr.admin.api.validation.Save;
import com.fitmgr.admin.api.validation.Update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User implements NoSensitiveObj<User>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Integer id;

    /**
     * 归属类型：0-租户，1-project
     */
    @ApiModelProperty(value = "归属类型", name = "affiliationType", required = true)
    private String affiliationType;

    /**
     * 用户名/账号
     */
    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", name = "username", required = true)
    private String username;

    /**
     * 密码
     */
    @JsonProperty
    @ApiModelProperty(value = "密码", name = "password", required = true)
    private String password;

    /**
     * 随机盐
     *
     * @JsonIgnore 禁止返回快照属性字段
     */
    @ApiModelProperty(value = "随机盐", name = "salt", required = false)
    private String salt;

    /**
     * 姓名/昵称
     */
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5][-_a-zA-Z0-9\\u4e00-\\u9fa5]{2,63}$", message = "用户名称格式错误:允许中文、大小写英文字母、数字、下划线、中划线（3-64位），且首位必须为英文字母、数字或中文", groups = {
            Save.class, Update.class })
    @NotBlank(message = "用户名称不能为空")
    @ApiModelProperty(value = "姓名", name = "name", required = true)
    private String name;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误：只允许大小开头为1的11位数字", groups = { Save.class, Update.class })
    @ApiModelProperty(value = "手机号", name = "phone", required = true)
    private String phone;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$", message = "邮箱格式错误：请输入正确的邮箱地址信息", groups = {
            Save.class, Update.class })
    @ApiModelProperty(value = "邮箱", name = "email", required = true)
    private String email;

    /**
     * 头像(提供默认头像)
     */
    @ApiModelProperty(value = "头像", name = "avatar", required = false)
    private String avatar;

    /**
     * 微信 openid
     */
    @ApiModelProperty(value = "微信 openid", name = "wxOpenid", required = false)
    private String wxOpenid;

    /**
     * QQ openid
     */
    @ApiModelProperty(value = "QQ openid", name = "qqOpenid", required = false)
    private String qqOpenid;

    /**
     * 默认VDC
     */
    private Integer defaultTenantId;

    /**
     * 状态：0-启用，1-禁用
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private String status;

    /**
     * 上一次登录时间
     */
    @ApiModelProperty(value = "上一次登录时间", name = "lastLoginTime", required = false)
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime", required = false)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间", name = "updateTime", required = false)
    private LocalDateTime updateTime;

    /**
     * 0-正常，1-删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除", name = "delFlag", required = false)
    private String delFlag;

    /**
     * 是否录入后还未修改密码
     */
    private Boolean newImport;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 是否锁定
     */
    private String lockState;

    /**
     * 密码过期时间
     */
    private LocalDateTime passExpirationTime;

    /**
     * 密码修改时间
     */
    private LocalDateTime passUpdateTime;

}
