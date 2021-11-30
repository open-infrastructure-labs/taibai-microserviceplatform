
package com.fitmgr.common.transaction.tx.springcloud.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.codingapi.tx.listener.service.InitService;

/**
 * @author Fitmgr
 * @since 4.1.0
 */
@Component
public class ServerListener implements ApplicationListener<WebServerInitializedEvent> {

    private Logger logger = LoggerFactory.getLogger(ServerListener.class);

    private int serverPort;

    @Autowired
    private InitService initService;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        logger.info("onApplicationEvent -> onApplicationEvent. " + event.getWebServer());
        this.serverPort = event.getWebServer().getPort();
        initService.start();
    }

    public int getPort() {
        return this.serverPort;
    }
}
