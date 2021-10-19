package com.fitmgr.admin.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.constants.PasswordTermEnum;
import com.fitmgr.admin.api.entity.PasswordTerm;
import com.fitmgr.admin.mapper.PasswordTermMapper;
import com.fitmgr.admin.service.IPasswordTermService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 密码有效期表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class PasswordTermServiceImpl extends ServiceImpl<PasswordTermMapper, PasswordTerm>
        implements IPasswordTermService {

    private final PasswordTermMapper passwordTermMapper;

    public LocalDateTime calculateExpirationTime() {
        PasswordTerm passwordTerm = passwordTermMapper.selectList(new QueryWrapper<PasswordTerm>().lambda()).get(0);
        LocalDateTime now = LocalDateTime.now();
        if (passwordTerm.getTerm().equals(PasswordTermEnum.ALWAYS.getCode())) {
            return null;
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.AWEEK.getCode())) {
            // 加1周
            return now.plusWeeks(1);
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.ONE_MONTH.getCode())) {
            // 加1月
            return now.plusMonths(1);
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.THREE_MONTHS.getCode())) {
            // 加3月
            return now.plusMonths(3);
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.HALF_YEAR.getCode())) {
            // 加半年
            return now.plusMonths(6);
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.AYEAR.getCode())) {
            // 加1年
            return now.plusYears(1);
        }
        return null;
    }

}
