/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */

package com.fitmgr.common.security.component;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.security.service.FitmgrUser;

/**
 * @author Fitmgr
 * @date 2019-09-17
 *       <p>
 *       token增强，客户端模式不增强。
 */
public class FitmgrTokenEnhancer implements TokenEnhancer {
    /**
     * Provides an opportunity for customization of an access token (e.g. through
     * its additional information map) during the process of creating a new token
     * for use by a client.
     *
     * @param accessToken    the current access token with its expiration and
     *                       refresh token
     * @param authentication the current authentication including client and user
     *                       details
     * @return a new token enhanced with additional information
     */
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        if (SecurityConstants.CLIENT_CREDENTIALS.equals(authentication.getOAuth2Request().getGrantType())) {
            return accessToken;
        }

        final Map<String, Object> additionalInfo = new HashMap<>(8);
        FitmgrUser fitmgrUser = (FitmgrUser) authentication.getUserAuthentication().getPrincipal();
        additionalInfo.put(SecurityConstants.DETAILS_USER_ID, fitmgrUser.getId());
        additionalInfo.put(SecurityConstants.DETAILS_USERNAME, fitmgrUser.getUsername());
        additionalInfo.put(SecurityConstants.DETAILS_EMAIL, fitmgrUser.getEmail());
        additionalInfo.put(SecurityConstants.DETAILS_TENANT_ID, fitmgrUser.getDefaultTenantId());
        additionalInfo.put(SecurityConstants.DETAILS_LICENSE, SecurityConstants.FITMGR_LICENSE);
        additionalInfo.put(SecurityConstants.ACTIVE, Boolean.TRUE);
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
