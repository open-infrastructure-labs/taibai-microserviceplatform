package com.taibai.admin.api.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 全量位置树节点
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LocationTreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String uuid;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private String state;

    /**
     * 删除标志
     */
    @JsonProperty("del_flag")
    private String delFlag;

    /**
     * 父节点id
     */
    @JsonProperty("parent_id")
    private String parentId;

    /**
     * 节点类型
     */
    private String type;

    /**
     * 云环境类型
     */
    @JsonProperty("cloud_platform_type")
    private String cloudPlatformType;

    /**
     * network_provider
     */
    @JsonProperty("network_provider")
    private String networkProvider;

    /**
     * 子节点
     */
    private List<LocationTreeNode> childs;

    public LocationTreeNode(String uuid, String name, String state, String delFlag, String parentId, String type,
            String cloudPlatformType, String networkProvider) {
        super();
        this.uuid = uuid;
        this.name = name;
        this.state = state;
        this.delFlag = delFlag;
        this.parentId = parentId;
        this.type = type;
        this.cloudPlatformType = cloudPlatformType;
        this.networkProvider = networkProvider;
    }
}
