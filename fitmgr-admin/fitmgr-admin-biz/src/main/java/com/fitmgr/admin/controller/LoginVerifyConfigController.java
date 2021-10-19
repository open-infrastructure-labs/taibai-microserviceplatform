package com.fitmgr.admin.controller;

import com.fitmgr.admin.api.entity.LoginVerifyConfig;
import com.fitmgr.admin.service.LoginVerifyConfigService;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/verify-config")
@Api(value = "verify-config", tags = "登录验证方式配置")
public class LoginVerifyConfigController {

    private final LoginVerifyConfigService loginVerifyConfigService;

    @ApiOperation(value = "查询登录验证方式")
    @GetMapping
    public R<LoginVerifyConfig> queryConfig() {
        return R.ok(loginVerifyConfigService.queryConfig());
    }


    @SysLog(value = "修改登录验证方式配置", cloudResType = "登录验证方式")
    @ApiOperation(value = "修改登录验证方式配置")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "verifyConfig", dataType = "VerifyConfig", required = true, value = "登录验证方式配置") })
    @PutMapping
    public R updateConfig(@RequestBody LoginVerifyConfig loginVerifyConfig) {
        loginVerifyConfigService.updateConfig(loginVerifyConfig);
        return R.ok();
    }
}
