package com.fitmgr.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VerifyInfoForFindPwd implements Serializable {
    private static final long serialVersionUID = -17504395935730682L;

    private String userName;

    private String email;

    private String randomStr;

    private String imageCode;
}
