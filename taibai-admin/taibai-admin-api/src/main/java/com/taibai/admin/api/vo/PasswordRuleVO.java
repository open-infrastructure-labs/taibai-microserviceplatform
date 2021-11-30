package com.taibai.admin.api.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.taibai.admin.api.entity.PasswordRule;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PasswordRuleVO extends PasswordRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 正则表达式
     */
    private String regular;
    
    /**
     * 校验提示
     */
    private String checkMsg;

    public PasswordRuleVO(PasswordRule passwordRule) {
        this.setId(passwordRule.getId());
        this.setMinLen(passwordRule.getMinLen());
        this.setMaxLen(passwordRule.getMaxLen());
        this.setComplexity(passwordRule.getComplexity());
    }
    
    
}
