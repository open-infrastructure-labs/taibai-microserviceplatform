
package com.fitmgr.common.gateway.configuration;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.PropertiesRouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.fitmgr.common.core.constant.CacheConstants;
import com.fitmgr.common.gateway.support.RouteCacheHolder;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/11/5
 *       <p>
 *       动态路由配置类
 */
@Slf4j
@Configuration
@ComponentScan("com.fitmgr.common.gateway")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class DynamicRouteAutoConfiguration {
    /**
     * 配置文件设置为空 redis 加载为准
     *
     * @return
     */
    @Bean
    public PropertiesRouteDefinitionLocator propertiesRouteDefinitionLocator() {
        return new PropertiesRouteDefinitionLocator(new GatewayProperties());
    }

    /**
     * redis 监听配置
     *
     * @param redisConnectionFactory redis 配置
     * @return
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener((message, bytes) -> {
            log.warn("接收到重新JVM 重新加载路由事件");
            RouteCacheHolder.removeRouteList();
        }, new ChannelTopic(CacheConstants.ROUTE_JVM_RELOAD_TOPIC));
        return container;
    }

    @Bean
    @ConditionalOnProperty(value = "spring.redis.cluster.enable", havingValue = "true")
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(
                redisProperties.getCluster().getNodes());

        // https://github.com/lettuce-io/lettuce-core/wiki/Redis-Cluster#user-content-refreshing-the-cluster-topology-view
        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh().enableAllAdaptiveRefreshTriggers().refreshPeriod(Duration.ofSeconds(5))
                .build();

        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions).build();

        // https://github.com/lettuce-io/lettuce-core/wiki/ReadFrom-Settings
        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.SLAVE_PREFERRED).clientOptions(clusterClientOptions).build();

        return new LettuceConnectionFactory(redisClusterConfiguration, lettuceClientConfiguration);
    }
}
