package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.LdapConfig;
import com.taibai.admin.mapper.LdapMapper;
import com.taibai.admin.service.ILdapService;

@Service
public class LdapServiceImpl extends ServiceImpl<LdapMapper, LdapConfig> implements ILdapService {


}
