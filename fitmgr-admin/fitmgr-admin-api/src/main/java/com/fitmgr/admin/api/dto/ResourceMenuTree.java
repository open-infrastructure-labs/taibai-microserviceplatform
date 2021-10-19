package com.fitmgr.admin.api.dto;

import com.fitmgr.admin.api.vo.ResourceMenuVO;
import com.fitmgr.common.core.util.TreeNode;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Classname ResourceMenuTree
 * @Description TODO
 * @Date 2019/11/16 17:28
 * @Created by DZL
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceMenuTree extends TreeNode {
    private String menuName;
    private boolean spread = false;
    private String menuCode;
    private String label;
    private String keepAlive;

    public ResourceMenuTree() {
    }

    public ResourceMenuTree(int id, String menuName, int parentId) {
        this.id = id;
        this.parentId = parentId;
        this.menuName = menuName;
        this.label = menuName;
    }

    public ResourceMenuTree(int id, String menuName, ResourceMenuTree parent) {
        this.id = id;
        this.parentId = parent.getId();
        this.menuName = menuName;
        this.label = menuName;
    }

    public ResourceMenuTree(ResourceMenuVO resourceMenuVO) {
        this.id = resourceMenuVO.getId();
        this.parentId = resourceMenuVO.getParentId();
        this.menuName = resourceMenuVO.getMenuName();
        this.label = resourceMenuVO.getMenuName();
    }
}
