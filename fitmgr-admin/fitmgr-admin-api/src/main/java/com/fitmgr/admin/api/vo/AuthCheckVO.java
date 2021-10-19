package com.fitmgr.admin.api.vo;

import com.fitmgr.admin.api.entity.AuthCheck;
import com.fitmgr.admin.api.entity.ResInfo;

import lombok.Data;

@Data
public class AuthCheckVO {
    /**
     * 资源信息
     */
    private ResInfo resInfo;
    /**
     * 权限信息
     */
    private AuthCheck authCheck;
}
