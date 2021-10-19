package com.fitmgr.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codingapi.tx.annotation.TxTransaction;
import com.fitmgr.admin.api.entity.Auth;
import com.fitmgr.admin.api.entity.Function;
import com.fitmgr.admin.mapper.AuthMapper;
import com.fitmgr.admin.mapper.FunctionMapper;
import com.fitmgr.admin.service.IFunctionService;
import com.fitmgr.common.core.util.R;

import lombok.AllArgsConstructor;

/**
 * <p>
 * 功能表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Service
@AllArgsConstructor
public class FunctionServiceImpl extends ServiceImpl<FunctionMapper, Function> implements IFunctionService {

    private final FunctionMapper functionMapper;
    private final AuthMapper authMapper;

    /**
     * 获取当前角色对应的菜单按钮展示
     */
    @Override
    public List<Function> getRoleFunction(Integer roleId, String menuId) {
        return functionMapper.getRoleFunction(roleId, menuId);
    }

    @TxTransaction
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R deletFunction(Integer functionId) {
        // 先删除权限表对应所有的functionId
        authMapper.delete(Wrappers.<Auth>lambdaQuery().eq(null != functionId, Auth::getFunctionId, functionId));
        // 再删除function
        return new R<>(functionMapper.deleteById(functionId));
    }

    @TxTransaction
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R updateCodefunction(List<Function> functions) {
        for (Function function : functions) {
            this.update(function,
                    Wrappers.<Function>lambdaUpdate().eq(null != function.getId(), Function::getId, function.getId()));
        }
        return R.ok();
    }
}
