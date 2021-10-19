package com.fitmgr.admin.api.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class LdapUser implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7099812164773614225L;

    private String username;

    private String userPassword;
    
    public String uid;
    
    public String displayName;
    
    public String mail;
    
    public String description;


}
