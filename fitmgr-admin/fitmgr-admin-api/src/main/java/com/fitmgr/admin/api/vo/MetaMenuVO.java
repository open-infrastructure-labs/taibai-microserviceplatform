package com.fitmgr.admin.api.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class MetaMenuVO implements Serializable {

    private static final long serialVersionUID = -269002381894345595L;
    private String title;
    private List<Integer> roles;
    private String icon;
    private Boolean noCache;
    private Boolean breadcrumb = true;
    private String activeMenu;
    private Boolean blank;
    private TemplateMenuVO template;
}
