
package com.fitmgr.common.security.service;

import java.util.Collection;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Fitmgr
 * @date 2018/8/20 扩展用户信息
 */
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FitmgrUser extends User {

    /**
     * 用户ID
     */
    @Getter
    private Integer id;
    /**
     * 部门ID
     */
    @Getter
    @Setter
    private String email;

    /**
     * 租户ID
     */
    @Getter
    @Setter
    private Integer defaultTenantId;

    /**
     * Construct the <code>User</code> with the details required by
     * {@link DaoAuthenticationProvider}.
     *
     * @param id                    用户ID
     * @param email                 部门ID
     * @param tenantId              租户ID
     * @param defaultRole           默认角色
     * @param defaultProject        默认project
     * @param username              the username presented to the
     *                              <code>DaoAuthenticationProvider</code>
     * @param password              the password that should be presented to the
     *                              <code>DaoAuthenticationProvider</code>
     * @param enabled               set to <code>true</code> if the user is enabled
     * @param accountNonExpired     set to <code>true</code> if the account has not
     *                              expired
     * @param credentialsNonExpired set to <code>true</code> if the credentials have
     *                              not expired
     * @param accountNonLocked      set to <code>true</code> if the account is not
     *                              locked
     * @param authorities           the authorities that should be granted to the
     *                              caller if they presented the correct username
     *                              and password and the user is enabled. Not null.
     * @throws IllegalArgumentException if a <code>null</code> value was passed
     *                                  either as a parameter or as an element in
     *                                  the <code>GrantedAuthority</code> collection
     */
    public FitmgrUser(Integer id, String email, Integer defaultTenantId, String username, String password,
            boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
        this.email = email;
        this.defaultTenantId = defaultTenantId;
    }
}
