
package com.taibai.admin.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.PropertiesRouteDefinitionLocator;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.taibai.admin.service.SysRouteConfService;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.gateway.support.DynamicRouteInitEvent;
import com.taibai.common.gateway.vo.RouteDefinitionVo;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Taibai
 * @date 2018/10/31
 *       <p>
 *       容器启动后保存配置文件里面的路由信息到Redis
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class DynamicRouteInitRunner {

    @Autowired
    private RedisTemplate redisTemplate;
    private final SysRouteConfService routeConfService;

    private final ThreadPoolTaskExecutor executor;

    @Async
    @Order
    @EventListener({ WebServerInitializedEvent.class, DynamicRouteInitEvent.class })
    public void initRoute() {
        executor.submit(() -> {
            Boolean result = redisTemplate.delete(CommonConstants.ROUTE_KEY);
            log.info("删除网关路由 {} ", result);
            try {
                routeConfService.routes().forEach(route -> {
                    if (route.getUri() != null) {
                        RouteDefinitionVo vo = new RouteDefinitionVo();
                        vo.setRouteName(route.getRouteName());
                        vo.setId(route.getRouteId());
                        vo.setUri(URI.create(route.getUri()));
                        vo.setOrder(route.getOrder());
                        JSONArray filterObj = JSONUtil.parseArray(route.getFilters());
                        vo.setFilters(filterObj.toList(FilterDefinition.class));
                        JSONArray predicateObj = JSONUtil.parseArray(route.getPredicates());
                        vo.setPredicates(predicateObj.toList(PredicateDefinition.class));
                        log.info("加载路由ID：{},{}", route.getRouteId(), vo);
                        redisTemplate
                                .setHashValueSerializer(new Jackson2JsonRedisSerializer<>(RouteDefinitionVo.class));
                        redisTemplate.opsForHash().put(CommonConstants.ROUTE_KEY, route.getRouteId(), vo);
                    }
                });
            } catch (Throwable th) {
                log.error("config route info fail", th);
            }
        });
        log.debug("初始化网关路由结束 ");
    }

    /**
     * 配置文件设置为空redis 加载的为准
     *
     * @return
     */
    @Bean
    public PropertiesRouteDefinitionLocator propertiesRouteDefinitionLocator() {
        return new PropertiesRouteDefinitionLocator(new GatewayProperties());
    }

    /**
     * 实例化Redis 解决乱码问题
     *
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> stringSerializerRedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        return redisTemplate;
    }
}
