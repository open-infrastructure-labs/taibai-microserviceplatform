package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.Ip;
import com.taibai.admin.mapper.IpMapper;
import com.taibai.admin.service.IIpService;

@Service
public class IpServiceImpl extends ServiceImpl<IpMapper, Ip> implements IIpService {


}
