package com.taibai.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.dto.SessionDTO;
import com.taibai.admin.api.entity.Session;
import com.taibai.admin.api.vo.SessionVO;

public interface ISessionService {
    /**
     * addSession
     * 
     * @param session
     */
    void addSession(Session session);

    /**
     * selectSessionByPage
     * 
     * @param page       page
     * @param sessionDTO sessionDTO
     * @return IPage<SessionVO>
     */
    IPage<SessionVO> selectSessionByPage(Page page, SessionDTO sessionDTO);

    /**
     * innerSelectSessionByPage
     * 
     * @param page page
     * @return IPage<Session>
     */
    IPage<Session> innerSelectSessionByPage(Page page);

    /**
     * querySessionByUserId
     * 
     * @param userId userId
     * @return List<Session>
     */
    List<Session> querySessionByUserId(Integer userId);

    /**
     * querySessionByToken
     * 
     * @param token token
     * @return Session
     */
    Session querySessionByToken(String token);

    /**
     * deleteSessionById
     * 
     * @param id id
     */
    void deleteSessionById(Integer id);

    /**
     * deleteByUserId
     * 
     * @param userId userId
     */
    void deleteByUserId(Integer userId);

    /**
     * deleteByToken
     * 
     * @param token token
     */
    void deleteByToken(String token);

    /**
     * kickout
     * 
     * @param id id
     */
    void kickout(Integer id);

    /**
     * kickoutByRestrictLoginTimeTask
     * 
     * @param id      id
     * @param endTime endTime
     */
    void kickoutByRestrictLoginTimeTask(Integer id, LocalDateTime endTime);

    /**
     * heartbeat
     * 
     * @param token token
     */
    void heartbeat(String token);

    /**
     * selectSessions
     * 
     * @param sessionDTO sessionDTO
     * @return List<SessionVO>
     */
    List<SessionVO> selectSessions(SessionDTO sessionDTO);
}
