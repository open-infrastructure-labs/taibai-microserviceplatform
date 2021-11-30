package com.taibai.admin.api.feign;

import java.util.List;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.entity.AccountLockStrategy;
import com.taibai.admin.api.entity.LockedAccountRecord;
import com.taibai.admin.api.entity.LoginFailRecord;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

/**
 * 创建人 dzl 创建时间 2020/2/29 描述
 **/

@FeignClient(contextId = "remoteAccountLockStrategyService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteAccountLockStrategyService {

    /**
     * 获取账号锁定策略配置
     *
     * @return R
     */
    @GetMapping("/AccountLockStrategy")
    public R<AccountLockStrategy> getAccountLockStrategy();

    /**
     * 新增登录失败记录
     * 
     * @param loginFailRecord loginFailRecord
     * @return R
     */
    @PostMapping("/AccountLockStrategy/LoginFailRecord")
    public R<Boolean> addLoginFailRecord(@Valid @RequestBody LoginFailRecord loginFailRecord);

    /**
     * 通过用户id获取登录失败记录
     * 
     * @param userId userId
     * @return R
     */
    @GetMapping("/AccountLockStrategy/LoginFailRecord/{userId}")
    public R<List<LoginFailRecord>> getLoginFailRecord(@PathVariable(name = "userId") Integer userId);

    /**
     * 删除24小时之前登录失败记录
     * 
     * @return R
     */
    @DeleteMapping("/AccountLockStrategy/LoginFailRecord")
    public R<Boolean> delLoginFailRecord();

    /**
     * 通过用户id删除登录失败记录
     * 
     * @param userId userId
     * @return R
     */
    @DeleteMapping("/AccountLockStrategy/LoginFailRecord/{userId}")
    public R<Boolean> delLoginFailRecord(@PathVariable(name = "userId") Integer userId);

    /**
     * 获取锁定用户记录
     * 
     * @return R
     */
    @GetMapping("/AccountLockStrategy/LockedAccountRecord")
    public R<List<LockedAccountRecord>> getLockedAccountRecord();

    /**
     * 新增锁定用户记录
     * 
     * @param lockedAccountRecord lockedAccountRecord
     * @return R
     */
    @PostMapping("/AccountLockStrategy/LockedAccountRecord")
    public R<Boolean> addLockedAccountRecord(@Valid @RequestBody LockedAccountRecord lockedAccountRecord);

    /**
     * 通过用户id获取锁定用户记录
     * 
     * @param userId userId
     * @return R
     */
    @GetMapping("/AccountLockStrategy/LockedAccountRecord/{userId}")
    public R<List<LockedAccountRecord>> getLockedAccountRecord(@PathVariable(name = "userId") Integer userId);
}
