package com.fitmgr.meterage.api.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsDTO implements Serializable {

    private Page<Map<String,String>> page;

    private String auth;

    private List<Integer> tableHeadList;

    private String tenant;

    private String project;

    private String service;

    private String statisticsFlag;
}
