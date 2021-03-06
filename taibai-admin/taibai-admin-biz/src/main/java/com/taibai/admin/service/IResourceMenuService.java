package com.taibai.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taibai.admin.api.dto.ResourceMenuTree;
import com.taibai.admin.api.entity.ResourceMenu;
import com.taibai.admin.api.vo.ResourceFunctionVO;
import com.taibai.common.core.util.R;

/**
 * <p>
 * 资源菜单表 服务类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
public interface IResourceMenuService extends IService<ResourceMenu> {

    /**
     * 返回资源树形菜单集合
     *
     * @return
     */
    List<ResourceMenuTree> getResourceMenu();

    /**
     * 添加资源菜单和对应所有操作项
     *
     * @param resourceFunctionVO 资源操作VO
     * @return
     */
    R saveResourceByFunction(ResourceFunctionVO resourceFunctionVO);

    /**
     * 通过code删除资源菜单
     *
     * @param menuCode 资源code
     * @return
     */
    R deletCodeResourceMenu(String menuCode);
}
