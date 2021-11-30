package com.taibai.admin.syncproject;

import lombok.Data;
import lombok.ToString;

import java.util.Objects;

@Data
@ToString
public class UserTenantRoleRow {
    String id;
    String account;
    String password;
    String name;
    String email;
    String phone;
    String role;
    String tenantName;
    String tenantEnName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTenantRoleRow that = (UserTenantRoleRow) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(tenantName, that.tenantName) &&
                Objects.equals(tenantEnName, that.tenantEnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, tenantName, tenantEnName);
    }
}
