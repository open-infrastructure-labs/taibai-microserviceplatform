package com.taibai.admin.api.vo;

import com.taibai.admin.api.dto.MenuTree;
import com.taibai.admin.api.entity.Subpage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubpageVO extends Subpage {
    private static final long serialVersionUID = -2407218527362773117L;
    private String menuName;
    private List<MenuTree> parentMenus;
    private List<SubpageVO> children;
}
