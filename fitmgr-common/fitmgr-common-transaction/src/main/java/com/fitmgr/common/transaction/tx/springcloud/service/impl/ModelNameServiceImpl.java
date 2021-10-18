
package com.fitmgr.common.transaction.tx.springcloud.service.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.codingapi.tx.listener.service.ModelNameService;
import com.fitmgr.common.transaction.tx.springcloud.listener.ServerListener;
import com.lorne.core.framework.utils.encode.MD5Util;

/**
 * @author Fitmgr
 * @date 2017/7/12.
 * @since 4.1.0
 */
@Service
@Configuration
public class ModelNameServiceImpl implements ModelNameService {

    @Value("${spring.application.name}")
    private String modelName;

    @Autowired
    private ServerListener serverListener;

    private String host = null;

    @Override
    public String getModelName() {
        return modelName;
    }

    private String getIp() {
        if (host == null) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return host;
    }

    private int getPort() {
        int port = serverListener.getPort();
        int count = 0;
        while (port == 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            port = serverListener.getPort();
            count++;

            if (count == 2000) {
                throw new RuntimeException("get server port error.");
            }
        }

        return port;
    }

    @Override
    public String getUniqueKey() {
        String address = getIp() + getPort();
        return MD5Util.md5(address.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getIpAddress() {
        String address = getIp() + ":" + getPort();
        return address;
    }
}
