package com.taibai.admin.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.constants.PasswordRuleComplexity;
import com.taibai.admin.api.entity.PasswordRule;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.PasswordRuleMapper;
import com.taibai.admin.service.IPasswordRuleService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 密码规则表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class PasswordRuleServiceImpl extends ServiceImpl<PasswordRuleMapper, PasswordRule>
        implements IPasswordRuleService {

    private final PasswordRuleMapper passwordRuleMapper;

    public void checkPassword(String password) {
        int a = 0;
        boolean b = true;
        // 使用正则表达式表示大写字母
        String regex = "(?=.*[A-Z])^.*$";
        Matcher m = Pattern.compile(regex).matcher(password);
        // 判断password中是否包含大写字母
        if (m.matches()) {
            a++;
        }
        // 使用正则表达式表示小写字母
        regex = "(?=.*[a-z])^.*$";
        m = Pattern.compile(regex).matcher(password);
        // 判断password中是否包含小写字母
        if (m.matches()) {
            a++;
        }
        // 使用正则表达式表示数字
        regex = "(?=.*[0-9])^.*$";
        m = Pattern.compile(regex).matcher(password);
        // 判断password中是否包含数字
        if (m.matches()) {
            a++;
        }
        // 使用正则表达式表示特殊字符
        regex = "(?=.*[~!@#$%^&*()_])^.*$";
        m = Pattern.compile(regex).matcher(password);
        // 判断password中是否包含特殊字符
        if (m.matches()) {
            a++;
        }
        // 使用正则表达式表示四种字符
        regex = "^[a-zA-Z0-9~!@#$%^&*()_]*$";
        m = Pattern.compile(regex).matcher(password);
        // 判断password中是否包含四种字符之外的字符
        if (m.matches()) {
            b = false;
        }
        PasswordRule passwordRule = passwordRuleMapper.selectList(new QueryWrapper<PasswordRule>().lambda()).get(0);
        if (password.length() < passwordRule.getMinLen()) {
            throw new UserCenterException("密码长度小于最小限制" + passwordRule.getMinLen());
        }
        if (password.length() > passwordRule.getMaxLen()) {
            throw new UserCenterException("密码长度大于最大限制" + passwordRule.getMaxLen());
        }
        if (a < Integer.parseInt(passwordRule.getComplexity()) || b) {
            Integer minLen = passwordRule.getMinLen();
            Integer maxLen = passwordRule.getMaxLen();
            String len;
            if (minLen.equals(maxLen)) {
                len = minLen + "";
            } else {
                len = minLen + "-" + maxLen;
            }
            throw new UserCenterException(
                    "密码支持" + len + "位" + PasswordRuleComplexity.getMsg(passwordRule.getComplexity()));
        }
    }

}
