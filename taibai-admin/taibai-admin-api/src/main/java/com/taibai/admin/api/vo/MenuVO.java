package com.taibai.admin.api.vo;

import com.taibai.admin.api.entity.Menu;
import com.taibai.admin.api.entity.Subpage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname MenuVO
 * @Description 菜单vo
 * @Date 2019/11/18 12:02
 * @Created by DZL
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MenuVO extends Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean hidden;
    private Boolean alwaysShow;
    private MetaMenuVO meta;
    private Boolean half;


}
