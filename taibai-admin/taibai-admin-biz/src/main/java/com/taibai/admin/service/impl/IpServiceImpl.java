package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.Ip;
import com.fitmgr.admin.mapper.IpMapper;
import com.fitmgr.admin.service.IIpService;

@Service
public class IpServiceImpl extends ServiceImpl<IpMapper, Ip> implements IIpService {


}
