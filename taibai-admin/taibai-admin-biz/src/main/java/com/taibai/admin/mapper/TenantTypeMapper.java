package com.taibai.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.entity.TenantType;
import com.taibai.admin.api.vo.TenantTypeVO;

/**
 * <p>
 * 租户类型表 Mapper 接口
 * </p>
 *
 * @author Taibai
 * @since 2019-11-14
 */
public interface TenantTypeMapper extends BaseMapper<TenantType> {

    /**
     * 分页查询租户类型列表
     * 
     * @param page 分页条件对象
     * @return 租户类型列表
     */
    IPage<TenantTypeVO> listPage(Page page);

}
