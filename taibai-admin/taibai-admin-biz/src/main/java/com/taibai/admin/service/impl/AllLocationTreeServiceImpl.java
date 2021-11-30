package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.AllLocationTree;
import com.taibai.admin.mapper.AllLocationTreeMapper;
import com.taibai.admin.service.IAllLocationTreeService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 全量位置树表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class AllLocationTreeServiceImpl extends ServiceImpl<AllLocationTreeMapper, AllLocationTree>
        implements IAllLocationTreeService {

    private final AllLocationTreeMapper allLocationTreeMapper;

}
