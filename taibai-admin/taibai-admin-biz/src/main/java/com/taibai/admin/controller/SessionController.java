package com.fitmgr.admin.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.dto.SessionDTO;
import com.fitmgr.admin.api.entity.Session;
import com.fitmgr.admin.api.entity.SessionConfig;
import com.fitmgr.admin.api.vo.SessionVO;
import com.fitmgr.admin.service.ISessionConfigService;
import com.fitmgr.admin.service.ISessionService;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/session")
@Api(value = "session", tags = "会话管理")
public class SessionController {

    private final ISessionService sessionService;

    private final ISessionConfigService sessionConfigService;

    @PostMapping
    public R addSession(@RequestBody Session session) {
        SessionConfig sessionConfig = sessionConfigService.querySessionConfig();
        session.setTimeout(TimeUnit.MINUTES.toMillis(sessionConfig.getSessionMaxValidMinutes()));
        sessionService.addSession(session);
        return R.ok();
    }

    @GetMapping("/{token}")
    public R<Session> selectSessionByToken(@PathVariable(name = "token") String token) {
        return R.ok(sessionService.querySessionByToken(token));
    }

    @ApiOperation(value = "分页条件查询会话列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = false, value = "分页条件"),
            @ApiImplicitParam(paramType = "query", name = "sessionDTO", dataType = "SessionDTO", required = false, value = "条件查询对象")})
    @GetMapping("/page")
    public R selectSessionByPage(Page page, SessionDTO sessionDTO) {
        return R.ok(sessionService.selectSessionByPage(page, sessionDTO));
    }

    @GetMapping("/querySessionByUserId/{userId}")
    public R<List<Session>> querySessionByUserId(@PathVariable(name = "userId") Integer userId) {
        if(userId == null) {
            return R.failed(BusinessEnum.PARAMETER_ID_NULL);
        }
        return R.ok(sessionService.querySessionByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public R deleteSessionById(@PathVariable(name = "id") Integer id) {
        if(id == null) {
            return R.failed(BusinessEnum.PARAMETER_ID_NULL);
        }
        sessionService.deleteSessionById(id);
        return R.ok();
    }

    @DeleteMapping("/deleteByUserId/{userId}")
    public R deleteByUserId(@PathVariable(name = "userId") Integer userId) {
        if(userId == null) {
            return R.failed(BusinessEnum.PARAMETER_ID_NULL);
        }
        sessionService.deleteByUserId(userId);
        return R.ok();
    }

    @DeleteMapping("/deleteByToken/{token}")
    public R deleteByToken(@PathVariable(name = "token") String token) {
        if(token == null) {
            return R.failed(BusinessEnum.PARAMETER_ID_NULL);
        }
        sessionService.deleteByToken(token);
        return R.ok();
    }

    @DeleteMapping("/kickout/{id}")
    public R kickout(@PathVariable(name = "id") Integer id){
        sessionService.kickout(id);
        return R.ok();
    }

    @GetMapping("/heartbeat/{token}")
    public R heartbeat(@PathVariable(name = "token") String token) {
        if(token == null) {
            return R.failed(BusinessEnum.PARAMETER_ID_NULL);
        }

        sessionService.heartbeat(token);
        return R.ok();
    }
    
    @ApiOperation(value = "条件查询会话列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "sessionDTO", dataType = "SessionDTO", required = false, value = "条件查询对象")})
    @GetMapping("/list")
    public R<List<SessionVO>> selectSessions(SessionDTO sessionDTO) {
        return R.ok(sessionService.selectSessions(sessionDTO));
    }
}
