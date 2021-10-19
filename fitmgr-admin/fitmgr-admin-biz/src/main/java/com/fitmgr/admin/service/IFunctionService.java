package com.fitmgr.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.entity.Function;
import com.fitmgr.common.core.util.R;

/**
 * <p>
 * 功能表 服务类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface IFunctionService extends IService<Function> {

    /**
     * 通过角色id和菜单id获取功能项
     *
     * @param roleId 角色id
     * @param menuId 菜单id
     * @return
     */
    List<Function> getRoleFunction(Integer roleId, String menuId);

    /**
     * 删除功能项
     *
     * @param functionId 功能id
     * @return
     */
    R deletFunction(Integer functionId);

    /**
     * 批量修改资源操作
     *
     * @param functions 操作List
     * @return
     */
    R updateCodefunction(List<Function> functions);

}
