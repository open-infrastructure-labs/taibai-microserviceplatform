
package com.taibai.admin.api.dto;

import java.util.List;

import com.taibai.admin.api.vo.MenuVO;
import com.taibai.admin.api.vo.MetaMenuVO;
import com.taibai.admin.api.vo.SubpageVO;
import com.taibai.common.core.util.SysTreeNode;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统菜单树
 *
 * @author Taibai
 * @since 2019-11-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuTree extends SysTreeNode {
    private static final long serialVersionUID = 6878916645719799087L;
    private String name;
    private String path;
    private String type;
    private String icon;
    private boolean spread = false;
    private String component;
    private Integer sort;
    private String authority;
    private String redirect;
    private String keepAlive;
    private String code;
    private String label;
    private String delFlag;
    private Boolean hidden;
    private MetaMenuVO meta;
    private Integer menuOrder;
    private List<SubpageVO> subpageList;
    private List<MenuTree> parentMenus;
    private Integer isExternal;
    private String externalLoginUrl;
    private String externalUsername;
    private String externalPwd;
    private Integer templateId;
    private String isGlobal;
    private List<Integer> tenantIds;
    private String status;

    public MenuTree() {
    }

    public MenuTree(String menuId, String name, String parentId) {
        this.menuId = menuId;
        this.parentId = parentId;
        this.name = name;
        this.label = name;
    }

    public MenuTree(String menuId, String name, MenuTree parent) {
        this.menuId = menuId;
        this.parentId = parent.getMenuId();
        this.name = name;
        this.label = name;
    }

    public MenuTree(MenuVO menuVo) {
        this.menuId = menuVo.getMenuId();
        this.parentId = menuVo.getParentId();
        this.icon = menuVo.getIcon();
        this.name = menuVo.getName();
        this.path = menuVo.getPath();
        this.component = menuVo.getComponent();
        this.type = menuVo.getType();
        this.label = menuVo.getName();
        this.sort = menuVo.getSort();
        this.keepAlive = menuVo.getKeepAlive();
        this.delFlag = menuVo.getDelFlag();
        this.isExternal = menuVo.getIsExternal();
        this.externalLoginUrl = menuVo.getExternalLoginUrl();
        this.externalUsername = menuVo.getExternalUsername();
        this.externalPwd = menuVo.getExternalPwd();
    }

}
