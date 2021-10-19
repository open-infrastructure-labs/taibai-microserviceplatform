
package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件管理
 *
 * @author Fitmgr
 * @date 2019-06-18 17:18:42
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFile extends Model<SysFile> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = true)
    private Long id;
    /**
     * 文件名
     */
    @TableField(value = "file_name")
    @ApiModelProperty(value = "/**", name = "fileName", required = true)
    private String fileName;
    /**
     * 原文件名
     */
    @ApiModelProperty(value = "原文件名", name = "original", required = true)
    private String original;
    /**
     * 容器名称
     */
    @ApiModelProperty(value = "容器名称", name = "bucketName", required = true)
    @TableField(value = "bucket_name")
    private String bucketName;
    /**
     * 文件类型
     */
    @ApiModelProperty(value = "文件类型", name = "type", required = true)
    private String type;
    /**
     * 文件大小
     */
    @ApiModelProperty(value = "文件大小", name = "fileSize", required = true)
    private Long fileSize;
    /**
     * 上传人
     */
    @ApiModelProperty(value = "上传人", name = "createUser", required = true)
    private String createUser;
    /**
     * 上传时间
     */
    @ApiModelProperty(value = "上传时间", name = "createTime", required = true)
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人", name = "updateUser", required = true)
    private String updateUser;
    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", name = "updateTime", required = true)
    private LocalDateTime updateTime;
    /**
     * 删除标识：1-删除，0-正常
     */
    @TableLogic
    @ApiModelProperty(value = "删除标识：1-删除，0-正常", name = "delFlag", required = true)
    private Integer delFlag;
}
