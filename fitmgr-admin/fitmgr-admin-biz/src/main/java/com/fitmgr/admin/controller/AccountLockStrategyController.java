package com.fitmgr.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.admin.api.constants.UserLockStatus;
import com.fitmgr.admin.api.entity.AccountLockStrategy;
import com.fitmgr.admin.api.entity.LockedAccountRecord;
import com.fitmgr.admin.api.entity.LoginFailRecord;
import com.fitmgr.admin.service.IAccountLockStrategyService;
import com.fitmgr.admin.service.ILockedAccountRecordService;
import com.fitmgr.admin.service.ILoginFailRecordService;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 账号锁定策略配置表 前端控制器
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/AccountLockStrategy")
@Api(value = "AccountLockStrategy", tags = "账号锁定策略配置模块")
public class AccountLockStrategyController {

    private final IAccountLockStrategyService iAccountLockStrategyService;
    private final ILockedAccountRecordService iLockedAccountRecordService;
    private final ILoginFailRecordService iLoginFailRecordService;

    @ApiOperation(value = "获取账号锁定策略配置")
    @GetMapping()
    public R<AccountLockStrategy> getAccountLockStrategy() {
        return R.ok(iAccountLockStrategyService.list().get(0));
    }

    @SysLog(value = "修改账号锁定策略配置", cloudResType = "账号锁定策略")
    @ApiOperation(value = "修改账号锁定策略配置")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "accountLockStrategy", dataType = "AccountLockStrategy", required = true, value = "账号锁定策略配置对象") })
    @PutMapping
    public R<Boolean> updateAccountLockStrategy(@Valid @RequestBody AccountLockStrategy accountLockStrategy) {
        return R.ok(iAccountLockStrategyService.updateById(accountLockStrategy));
    }

    @ApiOperation(value = "新增登录失败记录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "loginFailRecord", dataType = "LoginFailRecord", required = true, value = "登录失败对象") })
    @PostMapping("/LoginFailRecord")
    public R<Boolean> addLoginFailRecord(@Valid @RequestBody LoginFailRecord loginFailRecord) {
        return R.ok(iLoginFailRecordService.save(loginFailRecord));
    }

    @ApiOperation(value = "通过用户id获取登录失败记录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/LoginFailRecord/{userId}")
    public R<List<LoginFailRecord>> getLoginFailRecord(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            LoginFailRecord loginFailRecord = new LoginFailRecord();
            return R.ok(iLoginFailRecordService.list(Wrappers.query(loginFailRecord).eq("user_id", userId)
                    .eq("support_lock", UserLockStatus.SUPPORT_LOCK.getCode()).orderByDesc("fail_time")));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @ApiOperation(value = "删除24小时之前登录失败记录")
    @DeleteMapping("/LoginFailRecord")
    public R<Boolean> delLoginFailRecord() {
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.minusDays(1);
        return R.ok(iLoginFailRecordService
                .remove(new QueryWrapper<LoginFailRecord>().lambda().lt(LoginFailRecord::getFailTime, localDateTime)));
    }

    @ApiOperation(value = "通过用户id删除登录失败记录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @DeleteMapping("/LoginFailRecord/{userId}")
    public R<Boolean> delLoginFailRecord(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            return R.ok(iLoginFailRecordService
                    .remove(new QueryWrapper<LoginFailRecord>().lambda().eq(LoginFailRecord::getUserId, userId)));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @ApiOperation(value = "获取锁定用户记录")
    @GetMapping("/LockedAccountRecord")
    public R<List<LockedAccountRecord>> getLockedAccountRecord() {
        return R.ok(iLockedAccountRecordService.list(new QueryWrapper<LockedAccountRecord>().lambda()));
    }

    @ApiOperation(value = "新增锁定用户记录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "lockedAccountRecord", dataType = "LockedAccountRecord", required = true, value = "锁定用户对象") })
    @PostMapping("/LockedAccountRecord")
    public R<Boolean> addLockedAccountRecord(@Valid @RequestBody LockedAccountRecord lockedAccountRecord) {
        iLockedAccountRecordService.remove(new QueryWrapper<LockedAccountRecord>().lambda()
                .eq(LockedAccountRecord::getUserId, lockedAccountRecord.getUserId()));
        return R.ok(iLockedAccountRecordService.save(lockedAccountRecord));
    }

    @ApiOperation(value = "通过用户id获取锁定用户记录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/LockedAccountRecord/{userId}")
    public R<List<LockedAccountRecord>> getLockedAccountRecord(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            return R.ok(iLockedAccountRecordService
                    .list(new QueryWrapper<LockedAccountRecord>().lambda().eq(LockedAccountRecord::getUserId, userId)));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }
}
