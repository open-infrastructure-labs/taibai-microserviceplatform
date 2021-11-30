package com.taibai.admin.utils;

import com.alibaba.fastjson.JSON;
import com.taibai.admin.api.dto.TokenDTO;
import com.taibai.admin.api.entity.Session;
import com.taibai.admin.api.feign.RemoteTokenService;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.service.ISessionService;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.ResponseCodeEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;
import com.taibai.webpush.api.dto.SendMessageDto;
import com.taibai.webpush.api.entity.Mq2WebsocketMessageBean;
import com.taibai.webpush.api.feign.RemoteWebpushService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AdminUtils {

    @Autowired
    private RemoteWebpushService remoteWebpushService;

    @Autowired
    private ISessionService sessionService;

    @Autowired
    private RemoteTokenService remoteTokenService;

    public void sendEmail(Integer recvUserId, String title, String code, String parameters, List<String> address) {
        try {
            FitmgrUser loginUser = SecurityUtils.getUser();
            Integer userId = 1;
            Integer tenantId = -1;
            if(loginUser != null) {
                userId = loginUser.getId();
                tenantId = loginUser.getDefaultTenantId();
            }
            SendMessageDto sendMessageDto = new SendMessageDto();
            if (recvUserId == null) {
                sendMessageDto.setUsers(Lists.newArrayList(1));
                sendMessageDto.setUserId(1);
                sendMessageDto.setTenantId(-1);
            } else {
                sendMessageDto.setUsers(Lists.newArrayList(recvUserId));
                sendMessageDto.setUserId(userId);
                sendMessageDto.setTenantId(tenantId);
            }
            sendMessageDto.setTitle(title);
            sendMessageDto.setServiceName("taibai-admin");
            sendMessageDto.setCode(code);
            sendMessageDto.setMessageParameters(parameters);
            sendMessageDto.setAddressees(address);
            R r = remoteWebpushService.messageSend(sendMessageDto,SecurityConstants.FROM_IN);
            if (r.getCode() != ResponseCodeEnum.SUCCESS.getCode()) {
                log.error("发送邮件失败 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            } else {
                log.info("发送邮件成功 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            }
        } catch (Throwable th) {
            log.error("发送邮件失败", th);
        }
    }

    public void sendEmail(Integer recvUserId, String title, String code, Map<String, String> parameters, List<String> address) {
        try {
            FitmgrUser loginUser = SecurityUtils.getUser();
            Integer userId = 1;
            Integer tenantId = -1;
            if(loginUser != null) {
                userId = loginUser.getId();
                tenantId = loginUser.getDefaultTenantId();
            }
            SendMessageDto sendMessageDto = new SendMessageDto();
            sendMessageDto.setUsers(Lists.newArrayList(recvUserId));
            sendMessageDto.setUserId(userId);
            sendMessageDto.setTenantId(tenantId);
            sendMessageDto.setTitle(title);
            sendMessageDto.setServiceName("taibai-admin");
            sendMessageDto.setCode(code);
            sendMessageDto.setParameters(parameters);
            sendMessageDto.setAddressees(address);
            R r = remoteWebpushService.messageSend(sendMessageDto,SecurityConstants.FROM_IN);
            if (r.getCode() != ResponseCodeEnum.SUCCESS.getCode()) {
                log.error("发送邮件失败 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            } else {
                log.info("发送邮件成功 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            }
        } catch (Throwable th) {
            log.error("发送邮件失败", th);
        }
    }

    public void batchSendEmail(List<Integer> recvUserIds, String title, String code, String parameters, List<String> address) {
        try {
            FitmgrUser loginUser = SecurityUtils.getUser();
            SendMessageDto sendMessageDto = new SendMessageDto();
            sendMessageDto.setUsers(recvUserIds);
            sendMessageDto.setUserId(loginUser.getId());
            sendMessageDto.setTenantId(loginUser.getDefaultTenantId());
            sendMessageDto.setTitle(title);
            sendMessageDto.setServiceName("taibai-admin");
            sendMessageDto.setCode(code);
            sendMessageDto.setMessageParameters(parameters);
            sendMessageDto.setAddressees(address);
            R r = remoteWebpushService.messageSend(sendMessageDto);
            if (r.getCode() != ResponseCodeEnum.SUCCESS.getCode()) {
                log.error("发送邮件失败 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            } else {
                log.info("发送邮件成功 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            }
        } catch (Throwable th) {
            log.error("发送邮件失败", th);
        }
    }

    public void batchSendEmail(List<Integer> recvUserIds, String title, String code, Map<String, String> parameters, List<String> address) {
        try {
            FitmgrUser loginUser = SecurityUtils.getUser();
            SendMessageDto sendMessageDto = new SendMessageDto();
            sendMessageDto.setUsers(recvUserIds);
            sendMessageDto.setUserId(loginUser.getId());
            sendMessageDto.setTenantId(loginUser.getDefaultTenantId());
            sendMessageDto.setTitle(title);
            sendMessageDto.setServiceName("taibai-admin");
            sendMessageDto.setCode(code);
            sendMessageDto.setParameters(parameters);
            sendMessageDto.setAddressees(address);
            R r = remoteWebpushService.messageSend(sendMessageDto);
            if (r.getCode() != ResponseCodeEnum.SUCCESS.getCode()) {
                log.error("发送邮件失败 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            } else {
                log.info("发送邮件成功 sendMessageDto={}", JSON.toJSONString(sendMessageDto));
            }
        } catch (Throwable th) {
            log.error("发送邮件失败", th);
        }
    }

    public boolean kickoutByUserId(Integer userId, String message){
        List<Session> sessions = sessionService.querySessionByUserId(userId);

        if(CollectionUtils.isNotEmpty(sessions)) {
            for (Session session : sessions) {
                sendWebsocketMsg(message, session.getToken());

                TokenDTO tokenDTO = new TokenDTO();
                tokenDTO.setToken(session.getToken());
                remoteTokenService.kickout(tokenDTO);

                sessionService.deleteByToken(session.getToken());
            }
        }

        return true;
    }

    private void sendWebsocketMsg(String message, String token) {
        if(StringUtils.isEmpty(message)) {
            return;
        }
        Mq2WebsocketMessageBean mq2WebsocketMessageBean = new Mq2WebsocketMessageBean();
        mq2WebsocketMessageBean.setToken(token);
        mq2WebsocketMessageBean.setMessageType(2);
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("message", message);
        mq2WebsocketMessageBean.setMessageBody(messageBody);
        R r = remoteWebpushService.sendWebsocketMessage(mq2WebsocketMessageBean);
        log.info("sendWebsocketMessage return={}", JSON.toJSONString(r));
    }

    public boolean kickoutByUserIdExcludeToken(Integer userId, String token, String message){
        List<Session> sessions = sessionService.querySessionByUserId(userId);
        log.info("userId={} sessions.size()={}", userId, sessions.size());

        if(CollectionUtils.isNotEmpty(sessions)) {
            for (Session session : sessions) {
                log.info("token={} session.getToken()={}", token, session.getToken());
                if(StringUtils.equals(token, session.getToken())) {
                    continue;
                }
                log.info("sendWebsocketMsg message={} session.getToken()={}", message, session.getToken());
                sendWebsocketMsg(message, session.getToken());

                TokenDTO tokenDTO = new TokenDTO();
                tokenDTO.setToken(session.getToken());
                remoteTokenService.kickout(tokenDTO);

                sessionService.deleteByToken(session.getToken());
            }
        }
        return true;
    }

    public boolean kickoutByToken(String token, String message){
        sendWebsocketMsg(message, token);

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        remoteTokenService.kickout(tokenDTO);

        sessionService.deleteByToken(token);
        return true;
    }
}
