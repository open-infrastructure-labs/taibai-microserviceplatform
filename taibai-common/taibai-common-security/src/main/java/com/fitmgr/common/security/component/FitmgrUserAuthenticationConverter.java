
package com.fitmgr.common.security.component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.util.StringUtils;

import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.security.service.FitmgrUser;

/**
 * @author Fitmgr
 * @date 2019-03-07
 *       <p>
 *       根据checktoken 的结果转化用户信息
 */
public class FitmgrUserAuthenticationConverter implements UserAuthenticationConverter {
    private static final String N_A = "N/A";

    /**
     * Extract information about the user to be used in an access token (i.e. for
     * resource servers).
     *
     * @param authentication an authentication representing a user
     * @return a map of key values representing the unique information about the
     *         user
     */
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(USERNAME, authentication.getName());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

    /**
     * Inverse of {@link #convertUserAuthentication(Authentication)}. Extracts an
     * Authentication from a map.
     *
     * @param map a map of user information
     * @return an Authentication representing the user or null if there is none
     */
    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USERNAME)) {
            Collection<? extends GrantedAuthority> authorities = getAuthorities(map);

            String username = (String) map.get(USERNAME);
            Integer id = (Integer) map.get(SecurityConstants.DETAILS_USER_ID);
            String email = (String) map.get(SecurityConstants.DETAILS_EMAIL);
            Integer tenantId = (Integer) map.get(SecurityConstants.DETAILS_TENANT_ID);
            FitmgrUser user = new FitmgrUser(id, email, tenantId, username, N_A, true, true, true, true, authorities);
            return new UsernamePasswordAuthenticationToken(user, N_A, authorities);
        }
        return null;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
        Object authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(
                    StringUtils.collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}
