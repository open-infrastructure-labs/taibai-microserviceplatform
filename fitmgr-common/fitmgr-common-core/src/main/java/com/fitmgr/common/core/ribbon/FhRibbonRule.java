package com.fitmgr.common.core.ribbon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.core.config.EnvIsolationConfig;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.util.EnvIsoUtil;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhRibbonRule extends AbstractLoadBalancerRule {

    private static final String DEFAULT = "default";

    private DiscoveryClient discoveryClient;

    private EnvIsolationConfig envIsolationConfig;

    @Override
    public Server choose(Object o) {
        if (envIsolationConfig == null) {
            envIsolationConfig = SpringContextHolder.getBean(EnvIsolationConfig.class);
        }
        if (envIsolationConfig.getLogSwitch()) {
            log.info("choose object={}", o == null ? null : JSON.toJSONString(o));
            log.info("iLoadBalancer.getReachableServers()={}", getLoadBalancer().getReachableServers());
        }
        if (DEFAULT.equals(o)) {
            throw new RuntimeException("choose key invalid");
        }
        List<Server> serverList = getLoadBalancer().getReachableServers();
        if (CollectionUtils.isEmpty(serverList)) {
            log.info("serverList is empty");
            return null;
        }

        if (discoveryClient == null) {
            discoveryClient = SpringContextHolder.getBean(DiscoveryClient.class);
        }
        String headerEnv = this.getHeaderEnv(o);
        String sysEnv = EnvIsoUtil.getSysEnv();
        String appName = serverList.get(0).getMetaInfo().getAppName();
        String producerEnv = EnvIsoUtil.getProducerEnv(appName.toLowerCase());
        if (envIsolationConfig.getLogSwitch()) {
            log.info("appName={}", appName);
            log.info("producerEnv={}", producerEnv);
            log.info("headerEnv={}", headerEnv);
            log.info("sysEnv={}", sysEnv);
        }
        List<Server> toChooseServers = new ArrayList<>();
        List<Server> nullEnvServers = new ArrayList<>();

        Map<String, Server> serverMap = serverList.stream()
                .collect(Collectors.toMap(Server::getHost, a -> a, (k1, k2) -> k1));
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(appName);
        if (CollectionUtils.isEmpty(serviceInstances)) {
            return this.randomChoose(serverList);
        }

        for (ServiceInstance serviceInstance : serviceInstances) {
            if (envIsolationConfig.getLogSwitch()) {
                log.info("serviceInstance.getHost()={}", serviceInstance.getHost());
            }
            Server selectServer = serverMap.get(serviceInstance.getHost());
            if (selectServer == null) {
                log.info("selectServer == null");
                continue;
            }
            Map<String, String> metaData = serviceInstance.getMetadata();
            if (metaData == null || metaData.isEmpty()) {
                log.info("metaData == null");
                nullEnvServers.add(selectServer);
                continue;
            }
            String metaEnv = metaData.get(CommonConstants.ENV_HEADER_ENV_KEY);
            if (envIsolationConfig.getLogSwitch()) {
                log.info("metaEnv = {}", metaEnv);
            }
            if (StringUtils.isEmpty(metaEnv)) {
                nullEnvServers.add(selectServer);
                continue;
            }

            if (StringUtils.equals(producerEnv, metaEnv)) {
                toChooseServers.add(selectServer);
            } else if (StringUtils.equals(headerEnv, metaEnv)) {
                toChooseServers.add(selectServer);
            } else if (StringUtils.equals(sysEnv, metaEnv)) {
                toChooseServers.add(selectServer);
            }
        }

        if (CollectionUtils.isNotEmpty(toChooseServers)) {
            if (envIsolationConfig.getLogSwitch()) {
                log.info("toChooseServers={}", toChooseServers);
            }
            return this.randomChoose(toChooseServers);
        } else {
            if (CollectionUtils.isNotEmpty(nullEnvServers)) {
                if (envIsolationConfig.getLogSwitch()) {
                    log.info("nullEnvServers={}", nullEnvServers);
                }
                return this.randomChoose(nullEnvServers);
            }
        }

        if (envIsolationConfig.getLogSwitch()) {
            log.info("toChooseServers and nullEnvServers are empty");
        }
        return null;
    }

    private String getHeaderEnv(Object o) {
        try {
            if (o == null) {
                return null;
            }
            JSONObject keyJson = JSONObject.parseObject(JSON.toJSONString(o));
            JSONArray envArray = keyJson.getJSONArray(CommonConstants.ENV_HEADER_ENV_KEY);
            String fenghuoEnv = null;
            if (envArray != null && envArray.size() > 0) {
                fenghuoEnv = envArray.getString(0);
            }
            return fenghuoEnv;
        } catch (Throwable th) {
            log.error("getHeaderEnv fail. o={}", o, th);
        }
        return null;
    }

    private Server randomChoose(List<Server> toChooseServers) {
        int serverCount = toChooseServers.size();
        if (serverCount == 0) {
            return null;
        }

        int index = chooseRandomInt(serverCount);
        return toChooseServers.get(index);
    }

    private int chooseRandomInt(int serverCount) {
        return ThreadLocalRandom.current().nextInt(serverCount);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {

    }
}
