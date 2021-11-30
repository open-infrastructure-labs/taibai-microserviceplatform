package com.taibai.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ModifyInfoForRetrievePwd implements Serializable {
    private static final long serialVersionUID = 7075318401256366239L;
    private String signature;

    private Integer userId;

    private String newPassword;
}
