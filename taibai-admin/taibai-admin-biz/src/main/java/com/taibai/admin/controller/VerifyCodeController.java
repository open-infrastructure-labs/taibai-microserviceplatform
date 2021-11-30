package com.taibai.admin.controller;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taibai.admin.api.dto.VerifyCodeDTO;
import com.taibai.admin.api.validation.Save;
import com.taibai.admin.service.VerifyCodeService;
import com.taibai.common.core.util.R;
import com.taibai.common.log.annotation.SysLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/verify-code")
@Api(value = "verify-code", tags = "验证码")
public class VerifyCodeController {

    private final VerifyCodeService verifyCodeService;

    @SysLog(value = "登录发送验证码", cloudResType = "验证码")
    @ApiOperation(value = "登录发送验证码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "verifyCodeDTO", dataType = "VerifyCodeDTO", required = true, value = "验证码") })
    @PostMapping("/sendForLogin")
    public R sendVerifyCodeForLogin(@Validated(Save.class) @RequestBody VerifyCodeDTO verifyCodeDTO) {
        return verifyCodeService.sendVerifyCodeForLogin(verifyCodeDTO);
    }

    @GetMapping("/checkVerifyCode")
    public R checkVerifyCode(@RequestParam("userName") String userName, @RequestParam("randomCode") String randomCode,
            @RequestParam("verifyCode") String verifyCode) {
        return verifyCodeService.checkVerifyCodeForLogin(verifyCode, userName, randomCode);
    }

    @GetMapping("/randomCode")
    public R getRandomCode() {
        return R.ok(UUID.randomUUID());
    }
}
