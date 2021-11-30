package com.taibai.admin.task;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taibai.admin.api.constants.PasswordTermEnum;
import com.taibai.admin.api.entity.PassExpiration;
import com.taibai.admin.api.entity.PasswordTerm;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.PassExpirationMapper;
import com.taibai.admin.service.IPasswordTermService;
import com.taibai.admin.service.IUserService;
import com.taibai.common.core.util.R;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.job.api.core.biz.model.ReturnT;
import com.taibai.job.api.entity.Task;
import com.taibai.job.api.excutor.XxlBaseTaskExec;
import com.taibai.webpush.api.dto.SendMessageDto;
import com.taibai.webpush.api.feign.RemoteWebpushService;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PassExpirationTask extends XxlBaseTaskExec {

    private IPasswordTermService passwordTermService = SpringContextHolder.getBean(IPasswordTermService.class);
    private IUserService userService = SpringContextHolder.getBean(IUserService.class);
    private RemoteWebpushService remoteWebpushService = SpringContextHolder.getBean(RemoteWebpushService.class);
    private PassExpirationMapper passExpirationMapper = SpringContextHolder.getBean(PassExpirationMapper.class);

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        ReturnT returnt = new ReturnT();
        returnt.setCode(0);
        List<User> users = userService.list(new QueryWrapper<User>().lambda().eq(User::getDelFlag, "0"));
        PasswordTerm passwordTerm = passwordTermService.list().get(0);
        LocalDateTime now = LocalDateTime.now();
        long remindDays = 0;
        if (passwordTerm.getTerm().equals(PasswordTermEnum.ALWAYS.getCode())) {
            return returnt;
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.AWEEK.getCode())) {
            remindDays = 1;
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.ONE_MONTH.getCode())) {
            remindDays = 3;
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.THREE_MONTHS.getCode())) {
            remindDays = 7;
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.HALF_YEAR.getCode())) {
            remindDays = 7;
        } else if (passwordTerm.getTerm().equals(PasswordTermEnum.AYEAR.getCode())) {
            remindDays = 14;
        }
        for (User user : users) {
            if (user.getPassExpirationTime() == null) {
                continue;
            }
            Duration duration = Duration.between(now, user.getPassExpirationTime());
            // 相差的天数
            long days = duration.toDays();
            if (user.getPassExpirationTime().isAfter(now) && days > remindDays) {
                continue;
            }
            PassExpiration passExpiration = passExpirationMapper.selectOne(new QueryWrapper<PassExpiration>().lambda()
                    .eq(PassExpiration::getUserId, user.getId()).eq(PassExpiration::getDate, LocalDate.now()));
            if (passExpiration == null) {
                passExpiration = new PassExpiration();
                passExpiration.setUserId(user.getId());
                passExpiration.setDate(LocalDate.now());
            }
            if (passExpiration.getSendState() != null && passExpiration.getSendState().equals("0")) {
                continue;
            }
            String parameters = null;
            if (user.getPassExpirationTime().isAfter(now)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
                // LocalDateTime转Date
                LocalDateTime localDateTime = user.getPassExpirationTime();
                ZoneId zone = ZoneId.systemDefault();
                Instant instant = localDateTime.atZone(zone).toInstant();
                Date date = Date.from(instant);

                parameters = "密码将于" + sdf.format(date) + "到期，请及时修改";
            } else {
                parameters = "密码已过期，请立即修改";
            }
            SendMessageDto sendMessageDto = new SendMessageDto();
            sendMessageDto.setUsers(Lists.newArrayList(user.getId()));
            sendMessageDto.setUserId(1);
            sendMessageDto.setTenantId(-1);
            sendMessageDto.setTitle("密码到期提醒");
            sendMessageDto.setServiceName("taibai-admin");
            sendMessageDto.setCode("pass_expiration_remind");
            sendMessageDto.setMessageParameters("msg:" + parameters);
            sendMessageDto.setAddressees(Lists.newArrayList(user.getEmail()));
            R r = remoteWebpushService.messageSend(sendMessageDto);
            if (r.getCode() == 0) {
                passExpiration.setSendState("0");
            } else {
                throw new UserCenterException(r.getMsg());
            }
            if (passExpiration.getId() == null) {
                passExpirationMapper.insert(passExpiration);
            } else {
                passExpirationMapper.updateById(passExpiration);
            }
        }
        return returnt;
    }

    @Override
    public void taskRollback(Task task, Exception e) {

    }
}
