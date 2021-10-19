package com.fitmgr.admin.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.entity.Session;
import com.fitmgr.admin.api.vo.SessionVO;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

@FeignClient(contextId = "remoteSessionService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteSessionService {

    /**
     * addSession
     * 
     * @param session session
     * @return R
     */
    @PostMapping(value = { "/session" })
    R addSession(@RequestBody Session session);

    /**
     * querySessionByUserId
     * 
     * @param userId userId
     * @return R
     */
    @GetMapping("/session/querySessionByUserId/{userId}")
    R<List<Session>> querySessionByUserId(@PathVariable(name = "userId") Integer userId);

    /**
     * selectSessionByToken
     * 
     * @param token token
     * @return R
     */
    @GetMapping("/session/{token}")
    R<Session> selectSessionByToken(@PathVariable(name = "token") String token);

    /**
     * deleteSessionById
     * 
     * @param id id
     * @return R
     */
    @DeleteMapping("/session/{id}")
    R deleteSessionById(@PathVariable(name = "id") Integer id);

    /**
     * deleteByUserId
     * 
     * @param userId userId
     * @return R
     */
    @DeleteMapping("/session/deleteByUserId/{userId}")
    R deleteByUserId(@PathVariable(name = "userId") Integer userId);

    /**
     * deleteByToken
     * 
     * @param token token
     * @return R
     */
    @DeleteMapping("/session/deleteByToken/{token}")
    R deleteByToken(@PathVariable(name = "token") String token);

    /**
     * kickout
     * 
     * @param id id
     * @return R
     */
    @DeleteMapping("/session/kickout/{id}")
    R kickout(@PathVariable(name = "id") Integer id);

    /**
     * heartbeat
     * 
     * @param token token
     * @return R
     */
    @GetMapping("/session/heartbeat/{token}")
    R heartbeat(@PathVariable(name = "token") String token);

    /**
     * selectSessions
     * 
     * @return R
     */
    @GetMapping("/session/list")
    R<List<SessionVO>> selectSessions();
}
