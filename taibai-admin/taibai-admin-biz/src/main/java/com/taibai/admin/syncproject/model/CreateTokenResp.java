package com.fitmgr.admin.syncproject.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateTokenResp implements Serializable {
    private static final long serialVersionUID = -5816805802244107093L;

    private TokenDetail detail;
}
