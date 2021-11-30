package com.taibai.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taibai.admin.api.entity.Function;

/**
 * <p>
 * 功能表 Mapper 接口
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
public interface FunctionMapper extends BaseMapper<Function> {
    /**
     * getRoleFunction
     * 
     * @param roleId roleId
     * @param menuId menuId
     * @return List<Function>
     */
    List<Function> getRoleFunction(@Param("roleId") Integer roleId, @Param("menuId") String menuId);

}
