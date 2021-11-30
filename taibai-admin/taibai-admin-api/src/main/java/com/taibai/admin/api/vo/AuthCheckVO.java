package com.taibai.admin.api.vo;

import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.ResInfo;

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
