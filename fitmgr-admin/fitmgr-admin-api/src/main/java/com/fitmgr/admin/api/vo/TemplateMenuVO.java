package com.fitmgr.admin.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TemplateMenuVO implements Serializable {

    private static final long serialVersionUID = -1543349131997768199L;
    private Integer id;
    private String api = "api";
}
