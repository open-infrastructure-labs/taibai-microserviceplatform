package com.taibai.admin.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.dto.TenantTypeDTO;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.TenantType;
import com.taibai.admin.api.vo.TenantTypeVO;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.TenantTypeMapper;
import com.taibai.admin.service.ITenantTypeService;
import com.taibai.common.core.constant.CommonConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建人 mhp 创建时间 2019/11/14 描述
 **/

@Slf4j
@Service
@AllArgsConstructor
public class TenantTypeServiceImpl extends ServiceImpl<TenantTypeMapper, TenantType> implements ITenantTypeService {
    private final TenantTypeMapper tenantTypeMapper;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTenantType(TenantTypeDTO tenantTypeDTO) {
        TenantType tenantType = new TenantType();
        BeanUtils.copyProperties(tenantTypeDTO, tenantType);
        tenantType.setCreateTime(LocalDateTime.now());
        tenantType.setUpdateTime(tenantType.getCreateTime());
        tenantType.setDelFlag(CommonConstants.STATUS_NORMAL);
        tenantTypeMapper.insert(tenantType);
    }

    @Override
    public void updateTenantType(TenantTypeDTO tenantTypeDTO) {
        TenantType tenantType = new TenantType();
        BeanUtils.copyProperties(tenantTypeDTO, tenantType);
        tenantType.setUpdateTime(LocalDateTime.now());
        tenantTypeMapper.updateById(tenantType);
    }

    @Override
    public void deleteTenantType(Integer tenantTypeId) {
        Integer count = tenantMapper
                .selectCount(new QueryWrapper<Tenant>().lambda().eq(Tenant::getTypeId, tenantTypeId));
        if (count > 0) {
            throw new UserCenterException("存在租户引用该类型，不允许删除");
        }
        tenantTypeMapper.deleteById(tenantTypeId);
    }

    @Override
    public IPage<TenantTypeVO> selectListPage(Page page) {
        return tenantTypeMapper.listPage(page);
    }

}
