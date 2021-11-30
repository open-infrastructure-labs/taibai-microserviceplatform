package com.taibai.admin.api.vo;

import com.taibai.admin.api.entity.ResourceMenu;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname ResourceMenuVO
 * @Description 资源列表VO
 * @Date 2019/11/16 17:17
 * @Created by DZL
 */
@Data
public class ResourceMenuVO extends ResourceMenu implements Serializable {

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * menuId 相同则相同
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResourceMenuVO) {
            Integer targetResourceMenuId = ((ResourceMenuVO) obj).getId();
            return this.getId().equals(targetResourceMenuId);
        }
        return super.equals(obj);
    }

}
