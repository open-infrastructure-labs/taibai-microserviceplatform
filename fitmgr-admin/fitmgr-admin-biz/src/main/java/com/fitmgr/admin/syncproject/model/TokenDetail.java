package com.fitmgr.admin.syncproject.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

@Data
public class TokenDetail implements Serializable {
    private static final long serialVersionUID = 2346014699312706422L;

    @JSONField(name = "access_token")
    private String accessToken;
}
