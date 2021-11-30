package com.taibai.admin.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("ldap_config")
public class LdapConfig implements Serializable{

    private static final long serialVersionUID = 4367115691722893143L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", name = "id", required = false)
    private Long id;
    
    /**
     * 类型（0-OpenLDAP，1-AD）
     */
    @ApiModelProperty(value = "类型", name = "type", required = false)
    private Integer type;
    
    /**
     * 服务器地址
     */
    @ApiModelProperty(value = "服务器地址", name = "address", required = true)
    private String address;
    
    /**
     * 端口
     */
    @ApiModelProperty(value = "端口", name = "port", required = true)
    private Integer port;
    
    /**
     * BaseDn
     */
    @ApiModelProperty(value = "baseDn", name = "baseDn", required = true)
    private String baseDn;
    
    /**
     * 用户名
     */
    @ApiModelProperty(value = "账号", name = "username", required = true)
    private String username;
    
    /**
     * 用户名
     */
    @ApiModelProperty(value = "密码", name = "password", required = true)
    private String password;
    
    /**
     * uid（用来说明用户账号对应的是ldap中的哪个属性，一般为cn）
     */
    @ApiModelProperty(value = "uid", name = "uid", required = false)
    private String uid;
    
}
