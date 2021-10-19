package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.LdapConfig;
import com.fitmgr.admin.mapper.LdapMapper;
import com.fitmgr.admin.service.ILdapService;

@Service
public class LdapServiceImpl extends ServiceImpl<LdapMapper, LdapConfig> implements ILdapService {


}
