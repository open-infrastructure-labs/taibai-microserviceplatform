package com.fitmgr.admin.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fitmgr.admin.service.IProjectService;
import com.fitmgr.admin.service.ITenantService;
import com.fitmgr.admin.service.IUserService;
import com.fitmgr.common.core.constant.CommonConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class AdminRunner implements ApplicationRunner {

    private ITenantService tenantService;

    private IProjectService projectService;

    private IUserService userService;

    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        putBaseInfo();
    }

    /**
     * 将租户、project、用户中文名称写到redis中
     */
    private void putBaseInfo() {
        tenantService.list().forEach(tenant -> redisTemplate.opsForValue()
                .set(CommonConstants.TENANT_PREFIX + tenant.getId(), tenant.getName()));
        log.info("租户名称同步完成...");

        projectService.list().forEach(project -> redisTemplate.opsForValue()
                .set(CommonConstants.PROJECT_PREFIX + project.getId(), project.getName()));
        log.info("project名称同步完成...");

        userService.list().forEach(
                user -> redisTemplate.opsForValue().set(CommonConstants.USER_PREFIX + user.getId(), user.getName()));
        log.info("用户名称同步完成...");

    }
}
