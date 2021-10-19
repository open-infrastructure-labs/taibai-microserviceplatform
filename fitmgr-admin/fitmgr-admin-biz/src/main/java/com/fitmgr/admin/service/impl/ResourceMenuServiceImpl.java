package com.fitmgr.admin.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codingapi.tx.annotation.TxTransaction;
import com.fitmgr.admin.api.dto.ResourceMenuTree;
import com.fitmgr.admin.api.entity.Function;
import com.fitmgr.admin.api.entity.ResourceMenu;
import com.fitmgr.admin.api.vo.ResourceFunctionVO;
import com.fitmgr.admin.mapper.ResourceMenuMapper;
import com.fitmgr.admin.service.IFunctionService;
import com.fitmgr.admin.service.IResourceMenuService;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.core.util.TreeUtil;

import lombok.AllArgsConstructor;

/**
 * <p>
 * 资源菜单表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Service
@AllArgsConstructor
public class ResourceMenuServiceImpl extends ServiceImpl<ResourceMenuMapper, ResourceMenu>
        implements IResourceMenuService {

    private final IFunctionService functionService;

    @Override
    public List<ResourceMenuTree> getResourceMenu() {
        List<ResourceMenu> resourceMenus = baseMapper.selectList(Wrappers.emptyWrapper());
        List<ResourceMenuTree> trees = new ArrayList<>();
        ResourceMenuTree node;
        for (ResourceMenu menu : resourceMenus) {
            node = new ResourceMenuTree();
            node.setId(menu.getId());
            node.setParentId(menu.getParentId());
            node.setMenuName(menu.getMenuName());
            node.setMenuCode(menu.getMenuCode());
            node.setLabel(menu.getMenuName());
            trees.add(node);
        }
        return TreeUtil.build(trees, 0);
    }

    /**
     * 添加资源菜单和操作项
     *
     * @param resourceFunctionVO 资源操作VO
     * @return
     */
    @TxTransaction
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R saveResourceByFunction(ResourceFunctionVO resourceFunctionVO) {
        // 添加默认配置
        resourceFunctionVO.getResourceMenu().setParentId(1);
        // 添加一条资源菜单（组件的基础信息）
        boolean resource = this.save(resourceFunctionVO.getResourceMenu());
        resourceFunctionVO.getFunctions().stream()
                .forEach(function -> function.setResourceId(resourceFunctionVO.getResourceMenu().getId()));
        boolean function = functionService.saveBatch(resourceFunctionVO.getFunctions());
        if (resource && function) {
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.ADD_FAIL);
    }

    @TxTransaction
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R deletCodeResourceMenu(String menuCode) {
        // 通过code查id
        ResourceMenu resourceMenu = baseMapper.selectOne(Wrappers.<ResourceMenu>lambdaQuery()
                .eq(StringUtils.isNotEmpty(menuCode), ResourceMenu::getMenuCode, menuCode));
        // 删除资源菜单
        boolean b = this.removeById(resourceMenu.getId());
        // 删除资源对应的所有操作数据
        boolean remove = functionService.remove(Wrappers.<Function>lambdaQuery().eq(null != resourceMenu.getId(),
                Function::getResourceId, resourceMenu.getId()));
        if (b && remove) {
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.ADD_FAIL);
    }
}
