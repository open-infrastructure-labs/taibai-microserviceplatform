package com.fitmgr.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fitmgr.admin.api.constants.PasswordRuleComplexity;
import com.fitmgr.admin.api.constants.PasswordTermEnum;
import com.fitmgr.admin.api.entity.MaxHisPassCount;
import com.fitmgr.admin.api.entity.PasswordRule;
import com.fitmgr.admin.api.entity.PasswordTerm;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.vo.PasswordRuleVO;
import com.fitmgr.admin.service.IMaxHisPassCountService;
import com.fitmgr.admin.service.IPasswordRuleService;
import com.fitmgr.admin.service.IPasswordTermService;
import com.fitmgr.admin.service.IUserService;
import com.fitmgr.common.core.constant.enums.UserTypeEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 安全配置 前端控制器
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/safe")
@Api(value = "safe", tags = "安全配置模块")
public class SafeController {

    private final IMaxHisPassCountService imaxHisPassCountService;
    private final IPasswordRuleService ipasswordRuleService;
    private final IPasswordTermService ipasswordTermService;
    private final IUserService userService;

    @ApiOperation(value = "获取历史密码策略")
    @GetMapping("/MaxHisPassCount")
    public R<MaxHisPassCount> getMaxHisPassCount() {
        return R.ok(imaxHisPassCountService.list().get(0));
    }

    @SysLog(value = "修改历史密码策略", cloudResType = "安全配置")
    @ApiOperation(value = "修改历史密码策略")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "maxHisPassCount", dataType = "MaxHisPassCount", required = true, value = "历史密码策略对象") })
    @PutMapping("/MaxHisPassCount")
    public R<Boolean> updateMaxHisPassCount(@Valid @RequestBody MaxHisPassCount maxHisPassCount) {
        return R.ok(imaxHisPassCountService.updateById(maxHisPassCount));
    }

    @ApiOperation(value = "获取密码规则")
    @GetMapping("/PasswordRule")
    public R<PasswordRuleVO> getPasswordRule() {
        PasswordRule passwordRule = ipasswordRuleService.list().get(0);
        PasswordRuleVO passwordRuleVO = new PasswordRuleVO(passwordRule);
        Integer minLen = passwordRule.getMinLen();
        Integer maxLen = passwordRule.getMaxLen();
        String len;
        if (minLen.equals(maxLen)) {
            len = minLen + "";
        } else {
            len = minLen + "-" + maxLen;
        }
        String regular = null;
        String checkMsg = null;
        if (passwordRule.getComplexity().equals(PasswordRuleComplexity.SIMPLE.getCode())) {
            String regular1 = "(?=.*[A-Z])^.{" + minLen + "," + maxLen + "}$";
            String regular2 = "(?=.*[a-z])^.{" + minLen + "," + maxLen + "}$";
            String regular3 = "(?=.*[0-9])^.{" + minLen + "," + maxLen + "}$";
            String regular4 = "(?=.*[~!@#$%^&*()_])^.{" + minLen + "," + maxLen + "}$";
            regular = "(?=^[a-zA-Z0-9~!@#$%^&*()_]*$)((" + regular1 + ")|(" + regular2 + ")|(" + regular3 + ")|("
                    + regular4 + "))";
            checkMsg = "密码支持" + len + "位且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少1种";
        } else if (passwordRule.getComplexity().equals(PasswordRuleComplexity.STANDARD.getCode())) {
            String regular1 = "(?=.*[A-Z])(?=.*[a-z])^.{" + minLen + "," + maxLen + "}$";
            String regular2 = "(?=.*[A-Z])(?=.*[0-9])^.{" + minLen + "," + maxLen + "}$";
            String regular3 = "(?=.*[A-Z])(?=.*[~!@#$%^&*()_])^.{" + minLen + "," + maxLen + "}$";
            String regular4 = "(?=.*[a-z])(?=.*[0-9])^.{" + minLen + "," + maxLen + "}$";
            String regular5 = "(?=.*[a-z])(?=.*[~!@#$%^&*()_])^.{" + minLen + "," + maxLen + "}$";
            String regular6 = "(?=.*[0-9])(?=.*[~!@#$%^&*()_])^.{" + minLen + "," + maxLen + "}$";
            regular = "(?=^[a-zA-Z0-9~!@#$%^&*()_]*$)((" + regular1 + ")|(" + regular2 + ")|(" + regular3 + ")|("
                    + regular4 + ")|(" + regular5 + ")|(" + regular6 + "))";
            checkMsg = "密码支持" + len + "位且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少2种组合";
        } else if (passwordRule.getComplexity().equals(PasswordRuleComplexity.HIGH.getCode())) {
            String regular1 = "(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])^.{" + minLen + "," + maxLen + "}$";
            String regular2 = "(?=.*[A-Z])(?=.*[a-z])(?=.*[~!@#$%^&*()_])^.{" + minLen + "," + maxLen + "}$";
            String regular3 = "(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@#$%^&*()_])^.{" + minLen + "," + maxLen + "}$";
            String regular4 = "(?=.*[a-z])(?=.*[0-9])(?=.*[~!@#$%^&*()_])^.{" + minLen + "," + maxLen + "}$";
            regular = "(?=^[a-zA-Z0-9~!@#$%^&*()_]*$)((" + regular1 + ")|(" + regular2 + ")|(" + regular3 + ")|("
                    + regular4 + "))";
            checkMsg = "密码支持" + len + "位且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少3种组合";
        } else if (passwordRule.getComplexity().equals(PasswordRuleComplexity.STRONG.getCode())) {
            regular = "(?=^[a-zA-Z0-9~!@#$%^&*()_]*$)((?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[~!@#$%^&*()_])^.{" + minLen
                    + "," + maxLen + "}$)";
            checkMsg = "密码支持" + len + "位且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少4种组合";
        }
        passwordRuleVO.setRegular(regular);
        passwordRuleVO.setCheckMsg(checkMsg);
        return R.ok(passwordRuleVO);
    }

    @SysLog(value = "修改密码规则", cloudResType = "安全配置")
    @ApiOperation(value = "修改密码规则")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "passwordRule", dataType = "PasswordRule", required = true, value = "密码规则对象") })
    @PutMapping("/PasswordRule")
    public R<Boolean> updatePasswordRule(@Valid @RequestBody PasswordRule passwordRule) {
        return R.ok(ipasswordRuleService.updateById(passwordRule));
    }

    @ApiOperation(value = "获取密码有效期")
    @GetMapping("/PasswordTerm")
    public R<PasswordTerm> getPasswordTerm() {
        return R.ok(ipasswordTermService.list().get(0));
    }

    @SysLog(value = "修改密码有效期", cloudResType = "安全配置")
    @ApiOperation(value = "修改密码有效期")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "passwordTerm", dataType = "PasswordTerm", required = true, value = "密码有效期对象") })
    @PutMapping("/PasswordTerm")
    public R<Boolean> updatePasswordTerm(@Valid @RequestBody PasswordTerm passwordTerm) {
        if (passwordTerm.getTerm().equals(PasswordTermEnum.ALWAYS.getCode())) {
            List<User> users = userService
                    .list(new QueryWrapper<User>().lambda().isNotNull(User::getPassExpirationTime));
            for (User user : users) {
                user.setPassExpirationTime(LocalDateTime.parse("9999-12-31T23:59:59"));
                userService.updateById(user);
            }
        } else {
            List<User> users = userService
                    .list(new QueryWrapper<User>().lambda().ne(User::getUserType, UserTypeEnum.SYSTEM_INTERNAL.name()));
            for (User user : users) {
                LocalDateTime localDateTime = user.getPassUpdateTime() == null ? LocalDateTime.now()
                        : user.getPassUpdateTime();
                if (passwordTerm.getTerm().equals(PasswordTermEnum.AWEEK.getCode())) {
                    // 加1周
                    localDateTime = localDateTime.plusWeeks(1);
                } else if (passwordTerm.getTerm().equals(PasswordTermEnum.ONE_MONTH.getCode())) {
                    // 加1月
                    localDateTime = localDateTime.plusMonths(1);
                } else if (passwordTerm.getTerm().equals(PasswordTermEnum.THREE_MONTHS.getCode())) {
                    // 加3月
                    localDateTime = localDateTime.plusMonths(3);
                } else if (passwordTerm.getTerm().equals(PasswordTermEnum.HALF_YEAR.getCode())) {
                    // 加半年
                    localDateTime = localDateTime.plusMonths(6);
                } else if (passwordTerm.getTerm().equals(PasswordTermEnum.AYEAR.getCode())) {
                    // 加1年
                    localDateTime = localDateTime.plusYears(1);
                }
                user.setPassExpirationTime(localDateTime);
                userService.updateById(user);
            }
        }
        return R.ok(ipasswordTermService.updateById(passwordTerm));
    }
}
