package com.fitmgr.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckHrefInfoForRetrievePwd implements Serializable {
    private static final long serialVersionUID = -3574650151693801118L;

    private String signature;

    private Integer userId;
}
