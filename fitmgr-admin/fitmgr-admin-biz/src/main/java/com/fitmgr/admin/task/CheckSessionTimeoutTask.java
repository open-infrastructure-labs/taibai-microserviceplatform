package com.fitmgr.admin.task;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.dto.TokenDTO;
import com.fitmgr.admin.api.entity.Session;
import com.fitmgr.admin.api.entity.SessionConfig;
import com.fitmgr.admin.api.feign.RemoteTokenService;
import com.fitmgr.admin.service.ISessionConfigService;
import com.fitmgr.admin.service.ISessionService;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;
import com.fitmgr.webpush.api.entity.Mq2WebsocketMessageBean;
import com.fitmgr.webpush.api.feign.RemoteWebpushService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckSessionTimeoutTask extends XxlBaseTaskExec {

    private ISessionService sessionService = SpringContextHolder.getBean(ISessionService.class);
    private ISessionConfigService sessionConfigService = SpringContextHolder.getBean(ISessionConfigService.class);
    private RemoteTokenService remoteTokenService = SpringContextHolder.getBean(RemoteTokenService.class);
    private RemoteWebpushService remoteWebpushService = SpringContextHolder.getBean(RemoteWebpushService.class);
    private RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");

    private static final long MAX_INACTIVE_TIME = TimeUnit.HOURS.toMillis(1);

    public static final String SESSION_HEART_BEAT_KEY = "session:heartbeat:last:";

    public final String LAST_REQUEST_TIME = "session:request:last:";

    public static final String SESSION_OBJECT = "session:object:";

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        SessionConfig sessionConfig = sessionConfigService.querySessionConfig();
        int current = 1;
        int size = 100;
        Page page = new Page();
        page.setCurrent(current);
        page.setSize(size);
        while (true) {
            IPage<Session> iPage = sessionService.innerSelectSessionByPage(page);
            if (CollectionUtils.isEmpty(iPage.getRecords())) {
                break;
            }

            for (Session session : iPage.getRecords()) {
                if (isSessionTimeout(session, session.getTimeout())) {
                    kickoutSession(session);
                } else {
                    if (isSessionTimeout(session, MAX_INACTIVE_TIME) && isSessionInactive(session, sessionConfig)) {
                        kickoutSession(session);
                    }
                }
            }
            if (current == iPage.getPages()) {
                break;
            }
            current++;
        }
        ReturnT returnt = new ReturnT();
        returnt.setCode(0);
        return returnt;
    }

    private SessionConfig querySessionConfig() {
        return sessionConfigService.querySessionConfig();
    }

    private long convertDateStrToTimeStamp(LocalDateTime date) {
        return date.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    private boolean isSessionTimeout(Session session, Long timeout) {
        String dateStr = redisTemplate.opsForValue().get(LAST_REQUEST_TIME + session.getToken());
        if (StringUtils.isNotEmpty(dateStr)) {
            long timeStamp = convertDateStrToTimeStamp(LocalDateTime.parse(dateStr, DF));
            if (System.currentTimeMillis() - timeStamp >= timeout) {
                log.info("session timeout. session.getUserId()={}. token={}", session.getUserId(), session.getToken());
                return true;
            }
        } else {
            long timeStamp = convertDateStrToTimeStamp(session.getLoginTime());
            if (System.currentTimeMillis() - timeStamp >= timeout) {
                log.info("session timeout. session.getUserId()={}. token={}", session.getUserId(), session.getToken());
                return true;
            }
        }
        return false;
    }

    private boolean isSessionInactive(Session session, SessionConfig sessionConfig) {
        if (!sessionConfig.getCheckHeartbeat()) {
            return false;
        }

        String dateStr = redisTemplate.opsForValue()
                .get(CheckSessionTimeoutTask.SESSION_HEART_BEAT_KEY + session.getToken());
        if (StringUtils.isNotEmpty(dateStr)) {
            long timeStamp = convertDateStrToTimeStamp(LocalDateTime.parse(dateStr, DF));
            if (System.currentTimeMillis() - timeStamp >= MAX_INACTIVE_TIME) {
                log.info("session inactive. userid={} token={}", session.getUserId(), session.getToken());
                return true;
            }
        } else {
            long timeStamp = convertDateStrToTimeStamp(session.getLoginTime());
            if (System.currentTimeMillis() - timeStamp >= MAX_INACTIVE_TIME) {
                log.info("session inactive. userid={} token={}", session.getUserId(), session.getToken());
                return true;
            }
        }
        return false;
    }

    public boolean kickoutSession(Session session) {
        log.info("session timeout. kickout session. userid={} token={}", session.getUserId(), session.getToken());
        Mq2WebsocketMessageBean mq2WebsocketMessageBean = new Mq2WebsocketMessageBean();
        mq2WebsocketMessageBean.setToken(session.getToken());
        mq2WebsocketMessageBean.setMessageType(2);
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("message", "当前会话已超时, 请重新登录");
        mq2WebsocketMessageBean.setMessageBody(messageBody);
        remoteWebpushService.sendWebsocketMessage(mq2WebsocketMessageBean);

        if (!callAuthToKickout(session)) {
            log.error("callAuthToKickout fail token={}", session.getToken());
            return false;
        }
        sessionService.deleteSessionById(session.getId());
        redisTemplate.delete(CheckSessionTimeoutTask.SESSION_HEART_BEAT_KEY + session.getToken());
        return true;
    }

    public boolean kickoutSessionByRestrictLoginTimeTask(Session session, LocalDateTime endTime) {
        log.info("kickoutSessionByRestrictLoginTimeTask. kickout session. userid={} token={}", session.getUserId(),
                session.getToken());
        Mq2WebsocketMessageBean mq2WebsocketMessageBean = new Mq2WebsocketMessageBean();
        mq2WebsocketMessageBean.setToken(session.getToken());
        mq2WebsocketMessageBean.setMessageType(2);
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("message", "当前时间段不允许访问, 请" + endTime + "后重新登录");
        mq2WebsocketMessageBean.setMessageBody(messageBody);
        remoteWebpushService.sendWebsocketMessage(mq2WebsocketMessageBean);

        if (!callAuthToKickout(session)) {
            log.error("callAuthToKickout fail token={}", session.getToken());
            return false;
        }
        sessionService.deleteSessionById(session.getId());
        redisTemplate.delete(CheckSessionTimeoutTask.SESSION_HEART_BEAT_KEY + session.getToken());
        return true;
    }

    private Boolean callAuthToKickout(Session session) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(session.getToken());
        return remoteTokenService.kickout(tokenDTO).getData();
    }

    @Override
    public void taskRollback(Task task, Exception e) {

    }
}
