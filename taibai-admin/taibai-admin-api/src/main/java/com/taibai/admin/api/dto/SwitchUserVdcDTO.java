package com.taibai.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SwitchUserVdcDTO implements Serializable {
    private static final long serialVersionUID = -7281715988195046816L;

    private String userName;
    private String token;
    private String vdcName;
}
