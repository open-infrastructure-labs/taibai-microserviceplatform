package com.taibai.admin.runner;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.service.IUserService;
import com.taibai.admin.service.impl.PasswordTermServiceImpl;
import com.taibai.common.core.constant.enums.UserTypeEnum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class AdminUserRunner implements ApplicationRunner {

    private IUserService userService;
    private PasswordTermServiceImpl passwordTermServiceImpl;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<User> list = userService.list(new QueryWrapper<User>().lambda()
                .ne(User::getUserType, UserTypeEnum.SYSTEM_INTERNAL.name()).isNull(User::getPassUpdateTime));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = passwordTermServiceImpl.calculateExpirationTime();
        for (User user : list) {
            // 设置密码修改时间
            user.setPassUpdateTime(now);
            // 设置密码过期时间
            user.setPassExpirationTime(expiration);
            userService.updateById(user);
            log.info("用户{}密码过期时间已更新.", user.getUsername());
        }
    }
}
