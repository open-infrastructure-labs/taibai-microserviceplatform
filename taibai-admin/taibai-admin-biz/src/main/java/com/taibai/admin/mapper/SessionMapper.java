package com.taibai.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.dto.SessionDTO;
import com.taibai.admin.api.entity.Session;
import com.taibai.admin.api.vo.SessionVO;

public interface SessionMapper extends BaseMapper<Session> {
    /**
     * selectSessionByPage
     * 
     * @param page       page
     * @param sessionDTO sessionDTO
     * @return IPage<SessionVO>
     */
    IPage<SessionVO> selectSessionByPage(@Param("page") Page page, @Param("sessionDTO") SessionDTO sessionDTO);

    /**
     * innerSelectSessionByPage
     * 
     * @param page page
     * @return IPage<Session>
     */
    IPage<Session> innerSelectSessionByPage(@Param("page") Page page);

    /**
     * selectSessions
     * 
     * @param sessionDTO sessionDTO
     * @return List<SessionVO>
     */
    List<SessionVO> selectSessions(@Param("sessionDTO") SessionDTO sessionDTO);
}
