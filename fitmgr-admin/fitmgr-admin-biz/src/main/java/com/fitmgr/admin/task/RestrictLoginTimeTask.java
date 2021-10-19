package com.fitmgr.admin.task;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.admin.api.dto.SessionDTO;
import com.fitmgr.admin.api.entity.RestrictLloginTime;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.feign.RemoteSessionConfigService;
import com.fitmgr.admin.api.vo.SessionVO;
import com.fitmgr.admin.service.ISessionService;
import com.fitmgr.admin.service.IUserService;
import com.fitmgr.common.core.constant.enums.UserTypeEnum;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestrictLoginTimeTask extends XxlBaseTaskExec {

    private final String RESTRICT_LOGIN_TIME_SWITCHES_ON = "0";

    private IUserService userService = SpringContextHolder.getBean(IUserService.class);

    private ISessionService sessionService = SpringContextHolder.getBean(ISessionService.class);
    private RemoteSessionConfigService remoteSessionConfigService = SpringContextHolder
            .getBean(RemoteSessionConfigService.class);

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        RestrictLloginTime restrictLloginTime = remoteSessionConfigService.queryRestrictLloginTime().getData();
        LocalDateTime now = LocalDateTime.now();
        // 不允许访问时间段踢出所有用户
        if (RESTRICT_LOGIN_TIME_SWITCHES_ON.equals(restrictLloginTime.getSwitches())
                && restrictLloginTime.getStartTime().isBefore(now) && restrictLloginTime.getEndTime().isAfter(now)) {
            List<SessionVO> list = sessionService.selectSessions(new SessionDTO());
            for (SessionVO sessionVO : list) {
                User user = userService
                        .getOne(Wrappers.<User>query().lambda().eq(User::getUsername, sessionVO.getAccount()));
                // 系统用户不允许被踢出
                if (user == null || !UserTypeEnum.SYSTEM.toString().equals(user.getUserType())) {
                    sessionService.kickoutByRestrictLoginTimeTask(sessionVO.getId(), restrictLloginTime.getEndTime());
                }
            }
        }
        ReturnT returnt = new ReturnT();
        returnt.setCode(0);
        return returnt;
    }

    @Override
    public void taskRollback(Task task, Exception e) {

    }
}
