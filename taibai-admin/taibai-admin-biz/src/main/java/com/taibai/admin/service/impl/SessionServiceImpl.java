package com.taibai.admin.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.dto.SessionDTO;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.Session;
import com.taibai.admin.api.feign.RemoteAuthService;
import com.taibai.admin.api.vo.SessionVO;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.SessionMapper;
import com.taibai.admin.service.ISessionService;
import com.taibai.admin.task.CheckSessionTimeoutTask;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.constant.enums.OperatingRangeEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SessionServiceImpl implements ISessionService {

    private final SessionMapper sessionMapper;

    private final RemoteAuthService remoteAuthService;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addSession(Session session) {
        sessionMapper.insert(session);
        redisTemplate.opsForValue().set(CheckSessionTimeoutTask.SESSION_OBJECT + session.getToken(),
                JSON.toJSONString(session));
        redisTemplate.expire(CheckSessionTimeoutTask.SESSION_OBJECT + session.getToken(), 2, TimeUnit.HOURS);
    }

    @Override
    public IPage<SessionVO> selectSessionByPage(Page page, SessionDTO sessionDTO) {
        R<AuthCheck> r = remoteAuthService.newAuthCheck("select_sessions_page", null, null, null);
        if (r.getCode() == 0 && r.getData().isStatus()) {
            if (r.getData().getOperatingRange().equals(OperatingRangeEnum.ALL_CODE)) {
                return sessionMapper.selectSessionByPage(page, sessionDTO);
            } else {
                sessionDTO.setUserId(SecurityUtils.getUser().getId());
                return sessionMapper.selectSessionByPage(page, sessionDTO);
            }
        }
        throw new UserCenterException(BusinessEnum.AUTH_NOT);
    }

    @Override
    public IPage<Session> innerSelectSessionByPage(Page page) {
        return sessionMapper.innerSelectSessionByPage(page);
    }

    @Override
    public List<Session> querySessionByUserId(Integer userId) {
        return sessionMapper.selectList(new QueryWrapper<Session>().lambda().eq(Session::getUserId, userId));
    }

    @Override
    public Session querySessionByToken(String token) {
        String sessionStr = redisTemplate.opsForValue().get(CheckSessionTimeoutTask.SESSION_OBJECT + token);
        if (StringUtils.isEmpty(sessionStr)) {
            Session session = sessionMapper
                    .selectOne(new QueryWrapper<Session>().lambda().eq(Session::getToken, token));
            redisTemplate.opsForValue().set(CheckSessionTimeoutTask.SESSION_OBJECT + token, JSON.toJSONString(session));
            redisTemplate.expire(CheckSessionTimeoutTask.SESSION_OBJECT + token, 2, TimeUnit.HOURS);
            return session;
        }
        return JSONObject.parseObject(sessionStr, Session.class);
    }

    @Override
    public void deleteSessionById(Integer id) {
        sessionMapper.deleteById(id);
    }

    @Override
    public void deleteByUserId(Integer userId) {
        sessionMapper.delete(new QueryWrapper<Session>().lambda().eq(Session::getUserId, userId));
    }

    @Override
    public void deleteByToken(String token) {
        sessionMapper.delete(new QueryWrapper<Session>().lambda().eq(Session::getToken, token));
    }

    @Override
    public void kickout(Integer id) {
        Session session = sessionMapper.selectById(id);
        CheckSessionTimeoutTask checkSessionTimeoutTask = new CheckSessionTimeoutTask();
        checkSessionTimeoutTask.kickoutSession(session);
    }

    @Override
    public void kickoutByRestrictLoginTimeTask(Integer id, LocalDateTime endTime) {
        Session session = sessionMapper.selectById(id);
        CheckSessionTimeoutTask checkSessionTimeoutTask = new CheckSessionTimeoutTask();
        checkSessionTimeoutTask.kickoutSessionByRestrictLoginTimeTask(session, endTime);
    }

    @Override
    public void heartbeat(String token) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.now();
        String localTime = df.format(time);
        redisTemplate.opsForValue().set(CheckSessionTimeoutTask.SESSION_HEART_BEAT_KEY + token, localTime);
        redisTemplate.expire(CheckSessionTimeoutTask.SESSION_HEART_BEAT_KEY + token, 2, TimeUnit.HOURS);
    }

    @Override
    public List<SessionVO> selectSessions(SessionDTO sessionDTO) {
        return sessionMapper.selectSessions(sessionDTO);
    }
}
