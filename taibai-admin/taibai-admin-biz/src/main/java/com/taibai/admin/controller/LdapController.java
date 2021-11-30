package com.taibai.admin.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taibai.admin.api.entity.LdapConfig;
import com.taibai.admin.api.entity.LdapUser;
import com.taibai.admin.service.ILdapService;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.util.R;
import com.taibai.common.encrypt.util.AesUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/ldap")
@Api(value = "ldap", tags = "ldap")
public class LdapController {

    private ILdapService ldapService;

    /**
     * 
     * 根据id查询平台信息
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取Ldap信息")
    @GetMapping("/getConfigInfo")
    public R<LdapConfig> getConfigInfo() {
        try {
            List<LdapConfig> ldaps = ldapService.list();
            if (CollectionUtils.isEmpty(ldaps)) {
                return R.ok(new LdapConfig());
            }
            return R.ok(ldaps.get(0));
        } catch (Exception e) {
            log.error("获取Ldap信息" + e.getMessage(), e);
            return R.failed("获取Ldap信息异常");
        }
    }

    /**
     * 
     * 保存Ldap配置
     *
     * @param platform
     * @return
     */
    @ApiOperation(value = "保存Ldap配置")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "body", name = "LdapConfig", dataType = "LdapConfig", required = true, value = "Ldap配置"))
    @PostMapping("/save")
    public R add(@RequestBody LdapConfig config) {
        try {
            if (config.getId() == null) {
                setUid(config);
                return R.ok(ldapService.save(config));
            }
            return R.ok(ldapService.updateById(config));
        } catch (Exception e) {
            log.error("保存Ldap配置异常，config={}" + e.getMessage(), config, e);
            return R.failed("保存失败，请稍后重试");
        }
    }

    /**
     * 
     * 校验密码
     *
     * @param platform
     * @return
     */
    @ApiOperation(value = "鉴权")
    @PostMapping("/auth")
    public R auth(String username, String password) {
        try {
            R<LdapConfig> r = getConfigInfo();
            if (r == null || r.getCode() != CommonConstants.SUCCESS || r.getData().getId() == null) {
                log.error("用户[{}]Ldap鉴权失败，获取配置信息错误，res={}", username, r);
                return R.failed("获取Ldap配置失败");
            }
            boolean res = authenticate(username, password, r.getData());
            return res ? R.ok(true) : R.failed(false);
        } catch (Exception e) {
            log.error("用户[{}]Ldap鉴权失败" + e.getMessage(), username, e);
            return R.failed("Ldap鉴权失败");
        }
    }

    /**
     * 
     * 测试连接
     *
     * @param platform
     * @return
     */
    @ApiOperation(value = "测试Ldap配置")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "body", name = "LdapConfig", dataType = "LdapConfig", required = true, value = "Ldap配置"))
    @PostMapping("/testConnect")
    public R testConnect(@RequestBody LdapConfig config) {
        try {
            ldapConnect(config);
            return R.ok();
        } catch (Exception e) {
            log.error("测试Ldap连接失败，config={}" + e.getMessage(), config, e);
            return R.failed("连接失败");
        }
    }

    /**
     * 
     * 根据用户名查询用户信息
     *
     * @param username
     * @return
     */
    @ApiOperation(value = "根据用户名查询用户信息")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "username", dataType = "String", required = true, value = "用户名"))
    @GetMapping("/getByUsername")
    public R<LdapUser> getByUsername(String username) {
        List<LdapUser> users = getLdapUser(username);
        return CollectionUtils.isNotEmpty(users) ? R.ok(users.get(0)) : R.failed("用户不存在");
    }

    /**
     * 
     * 查询用户列表
     *
     * @param username
     * @return
     */
    @ApiOperation(value = "查询用户列表")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "username", dataType = "String", required = true, value = "用户名"))
    @GetMapping("/getAllUsers")
    public List<LdapUser> getAllLdapUser(String username) {
        return getLdapUser("*");
    }
    
    /**
     * 
     * 设置默认属性
     *
     * @param config
     */
    private void setUid(LdapConfig config) {
        if (StringUtils.isBlank(config.getUid())) {
            config.setUid(config.getType() == 0 ? "cn" : "sAMAccountName");
        }
    }
    
    /**
     * 
     * 获取ldap连接对象
     *
     * @param config
     * @return
     * @throws Exception
     */
    private LdapContext ldapConnect(LdapConfig config) throws Exception{
        // 此处url格式为：ldap://{ip}:{端口}
        String url = "ldap://" + config.getAddress() + ":" + config.getPort();
        String factory = "com.sun.jndi.ldap.LdapCtxFactory";
        String simple = "simple";
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, factory);
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, simple);
        env.put(Context.SECURITY_PRINCIPAL, config.getUsername());
        env.put(Context.SECURITY_CREDENTIALS, AesUtil.decrypt(config.getPassword()));
        LdapContext ctx = null;
        Control[] connCtls = null;
        ctx = new InitialLdapContext(env, connCtls);
        log.info( "server[{}]认证成功", config.getAddress()); 
        return ctx;
    }

    /**
     * 
     * 用户名密码鉴权
     *
     * @param username
     * @param password
     * @param config
     * @return
     */
    private boolean authenticate(String username, String password, LdapConfig config) {
        // 此处url格式为：ldap://{ip}:{端口}
        String url = "ldap://" + config.getAddress() + ":" + config.getPort();

        LdapContextSource cs = new LdapContextSource();
        cs.setCacheEnvironmentProperties(false);
        cs.setUrl(url);
        cs.setAuthenticationSource(new AuthenticationSource() {
            @Override
            public String getCredentials() {
                // 连接密码
                return AesUtil.decrypt(config.getPassword());
            }

            @Override
            public String getPrincipal() {
                // 连接dn
                return config.getUsername();
            }
        });

        LdapTemplate ldapTemplate = new LdapTemplate(cs);
        try {
            EqualsFilter filter = new EqualsFilter(config.getUid(), username);
            ldapTemplate.setIgnorePartialResultException(true);
            boolean flag = ldapTemplate.authenticate(config.getBaseDn(), filter.toString(), password);
            log.info("用户[{}]Ldap验证解结果：{}", username, flag);
            return flag;
        } catch (Exception e) {
            log.info( "用户[{}]Ldap鉴权失败" + e.getMessage(), username, e); 
        }
        return false;
    }

    /**
     * 
     * 根据条件查询用户
     *
     * @param username
     * @return
     */
    private List<LdapUser> getLdapUser(String username) {
        List<LdapUser> lm = new ArrayList<LdapUser>();
        try {
            R<LdapConfig> res =  getConfigInfo();
            if (res.getCode() != 0 || res.getData() == null || res.getData().getId() == null) {
                log.error("根据[{}]查询用户失败，获取配置信息错误， res={}", username, res);
                return lm;
            }
            LdapConfig config = res.getData();
            
            LdapContext ctx = ldapConnect(res.getData());
             if(ctx != null){
                //过滤条件
                String filter = "(&(objectClass=*)(" + config.getUid() + "=" + username + "))";
                String[] attrPersonArray = { "uid", "userPassword", "displayName", "cn", "sn", "mail", "description", config.getUid()};
                SearchControls searchControls = new SearchControls();//搜索控件
                searchControls.setSearchScope(2);//搜索范围
                searchControls.setReturningAttributes(attrPersonArray);
                //1.要搜索的上下文或对象的名称；2.过滤条件，可为null，默认搜索所有信息；3.搜索控件，可为null，使用默认的搜索控件
                NamingEnumeration<SearchResult> answer = ctx.search(config.getBaseDn(), filter.toString(), searchControls);
                while (answer.hasMore()) {
                    SearchResult result = (SearchResult) answer.next();
                    NamingEnumeration<? extends Attribute> attrs = result.getAttributes().getAll();
                    LdapUser lu = new LdapUser();
                    while (attrs.hasMore()) {

                        Attribute attr = (Attribute) attrs.next();
                        if (config.getUid().equals(attr.getID())) {
                            lu.setUsername(attr.get().toString());
                        } else if("userPassword".equals(attr.getID())){
                            Object value = attr.get();
                            lu.setUserPassword(new String((byte[]) value, StandardCharsets.UTF_8));
                        } else if ("uid".equals(attr.getID())) {
                            lu.setUid(attr.get().toString());
                        } else if ("displayName".equals(attr.getID())) {
                            lu.setDisplayName(attr.get().toString());
                        } else if ("cn".equals(attr.getID())) {
                            lu.setUsername(attr.get().toString());
                        } else if ("mail".equals(attr.getID())) {
                            lu.setMail(attr.get().toString());
                        } else if ("description".equals(attr.getID())) {
                            lu.setDescription(attr.get().toString());
                        }
                    }
                    if(StringUtils.isNotBlank(lu.getUsername()))
                        lm.add(lu);

                }
            }
        } catch (Exception e) {
            log.error("获取用户信息异常:username={}" + e.getMessage(), username, e);
        }

        return lm;
    }
}
