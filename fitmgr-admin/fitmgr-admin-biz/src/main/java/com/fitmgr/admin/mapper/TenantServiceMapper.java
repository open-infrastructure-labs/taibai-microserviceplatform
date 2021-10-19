package com.fitmgr.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.admin.api.entity.TenantService;

/**
 * <p>
 * 租户服务表 Mapper 接口
 * </p>
 *
 * @author Fitmgr
 * @since 2021-01-14
 */
public interface TenantServiceMapper extends BaseMapper<TenantService> {
    /**
     * 批量插入
     *
     * @param list      租户id集合
     * @param serviceId
     * @return 成功个数
     */
    int insertBatch(@Param("list") List<Integer> list, @Param("serviceId") String serviceId);

    /**
     * 根据服务模板id删除
     *
     * @param serviceId 服务模板id
     * @return 成功个数
     */
    int deleteByServiceId(@Param("serviceId") String serviceId);
}
