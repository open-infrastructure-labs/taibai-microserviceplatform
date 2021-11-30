package com.taibai.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    public void configure(WebSecurity web) throws Exception {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // "\\", "%5c", "%5C"
        firewall.setAllowBackSlash(true);
        // ";", "%3b", "%3B"
        firewall.setAllowSemicolon(true);
        // "//", "%2f%2f", "%2f%2F", "%2F%2f", "%2F%2F"
        firewall.setAllowUrlEncodedDoubleSlash(true);
        // "%25", "%"
        firewall.setAllowUrlEncodedPercent(true);
        // "%2e", "%2E"
        firewall.setAllowUrlEncodedPeriod(true);
        // "%2f", "%2F"
        firewall.setAllowUrlEncodedSlash(true);
        //加入自定义的防火墙
        web.httpFirewall(firewall);
        super.configure(web);
    }
}