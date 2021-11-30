package com.taibai.common.core.util;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: DZL
 * @Date: 2019/11/25
 * @Description:
 */
@Data
public class SysTreeNode implements Serializable {
    private static final long serialVersionUID = 5199118942719566742L;
    protected String menuId;
    protected String parentId;

    /**
     * 路由菜单 是否为父菜单显示
     */
    private Boolean alwaysShow;
    protected List<SysTreeNode> children = new ArrayList<SysTreeNode>();

    public void add(SysTreeNode node) {
        children.add(node);
    }
}
