package com.fitmgr.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VerifyCodeDTO implements Serializable {
    private static final long serialVersionUID = -3553600821067628079L;

    private String codeType;

    private String randomCode;

    private String userName;
}
