
package com.fitmgr.common.transaction.rewrite;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;

import com.codingapi.tx.config.service.TxManagerTxUrlService;
import com.fitmgr.common.core.constant.ServiceNameConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/8/19 使用服务发现重写 Txmanager 获取规则
 */
@Slf4j
@Service
@AllArgsConstructor
public class TxManagerTxUrlServiceImpl implements TxManagerTxUrlService {
    private final LoadBalancerClient loadBalancerClient;

    @Override
    public String getTxUrl() {
        ServiceInstance serviceInstance = loadBalancerClient.choose(ServiceNameConstants.TX_MANAGER);
        String host = serviceInstance.getHost();
        Integer port = serviceInstance.getPort();
        String url = String.format("http://%s:%s/tx/manager/", host, port);
        log.info("tm.manager.url -> {}", url);
        return url;
    }
}
