package com.fitmgr.meterage.service.impl;

import com.fitmgr.meterage.api.entity.MeterageItem;
import com.fitmgr.meterage.mapper.MeterageItemMapper;
import com.fitmgr.meterage.service.IMeterageItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 计量项表 服务实现类
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
@Slf4j
@Service
@AllArgsConstructor
public class MeterageItemServiceImpl extends ServiceImpl<MeterageItemMapper, MeterageItem> implements IMeterageItemService {

}
