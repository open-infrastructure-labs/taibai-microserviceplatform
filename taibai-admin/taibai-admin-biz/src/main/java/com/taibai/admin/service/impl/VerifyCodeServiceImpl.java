package com.taibai.admin.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taibai.admin.api.constants.VerifyTypeEnum;
import com.taibai.admin.api.dto.VerifyCodeDTO;
import com.taibai.admin.api.entity.LoginVerifyConfig;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.mapper.LoginVerifyConfigMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.service.VerifyCodeService;
import com.taibai.admin.utils.AdminUtils;
import com.taibai.common.core.util.R;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private final RedisTemplate linkRedisTemplate;

    private final LoginVerifyConfigMapper loginVerifyConfigMapper;
    private final UserMapper userMapper;
    private final AdminUtils adminUtils;

    @Override
    public R sendVerifyCodeForLogin(VerifyCodeDTO verifyCodeDTO) {
        LoginVerifyConfig loginVerifyConfig = loginVerifyConfigMapper.selectById(1);
        if (!VerifyTypeEnum.PASSWORD_MAIL.name().equals(loginVerifyConfig.getVerifyType())) {
            return R.failed("邮箱验证码登录关闭，请刷新页面重新登录");
        }
        User user = userMapper
                .selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, verifyCodeDTO.getUserName()));
        if (user == null) {
            return R.failed("邮箱验证码发送失败");
        }
        String verifyCode = String.valueOf(new Random().nextInt(899999) + 100000);
        ValueOperations<String, String> operations = linkRedisTemplate.opsForValue();
        operations.set("verifycode:" + verifyCodeDTO.getUserName() + ":" + verifyCodeDTO.getRandomCode(), verifyCode,
                10, TimeUnit.MINUTES);
        if (VerifyTypeEnum.PASSWORD_MAIL.name().equals(loginVerifyConfig.getVerifyType())) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("verificationCode", verifyCode);
            map.put("userName", verifyCodeDTO.getUserName());
            adminUtils.sendEmail(user.getId(), "登录验证码", "login-verification-code", map,
                    Lists.newArrayList(user.getEmail()));
        }
        return R.ok();
    }

    @Override
    public R checkVerifyCodeForLogin(String verifyCode, String userName, String randomCode) {
        if (StringUtils.isEmpty(verifyCode) || "null".equals(verifyCode)) {
            return R.ok(false, "请输入邮箱验证码或刷新页面重新登录");
        }
        ValueOperations<String, String> operations = linkRedisTemplate.opsForValue();
        String cacheVerifyCode = operations.get("verifycode:" + userName + ":" + randomCode);
        if (StringUtils.isEmpty(cacheVerifyCode)) {
            return R.ok(false, "邮箱验证码错误或过期，请重新获取");
        }
        if (!StringUtils.equals(cacheVerifyCode, verifyCode)) {
            return R.ok(false, "邮箱验证码错误");
        }
        return R.ok(true);
    }
}
