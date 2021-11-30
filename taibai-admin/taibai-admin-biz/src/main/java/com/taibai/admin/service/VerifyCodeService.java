package com.taibai.admin.service;

import com.taibai.admin.api.dto.VerifyCodeDTO;
import com.taibai.common.core.util.R;

public interface VerifyCodeService {
    /**
     * sendVerifyCodeForLogin
     * 
     * @param verifyCodeDTO verifyCodeDTO
     */
    R sendVerifyCodeForLogin(VerifyCodeDTO verifyCodeDTO);

    /**
     * checkVerifyCodeForLogin
     * 
     * @param verifyCode verifyCode
     * @param userName   userName
     * @param randomCode randomCode
     * @return R
     */
    R checkVerifyCodeForLogin(String verifyCode, String userName, String randomCode);
}
