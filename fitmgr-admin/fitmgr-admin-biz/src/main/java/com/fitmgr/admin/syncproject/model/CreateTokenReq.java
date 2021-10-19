package com.fitmgr.admin.syncproject.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateTokenReq implements Serializable {
    private static final long serialVersionUID = 6626528823552534440L;

    private String appId;

    private String appSecret;
}
