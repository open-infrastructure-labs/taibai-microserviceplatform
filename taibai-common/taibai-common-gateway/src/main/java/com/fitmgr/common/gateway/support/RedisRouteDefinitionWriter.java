
package com.taibai.common.gateway.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.common.gateway.vo.RouteDefinitionVo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Taibai
 * @date 2018/10/31
 *       <p>
 *       redis 保存路由信息，优先级比配置文件高
 */
@Slf4j
@Component
@AllArgsConstructor
public class RedisRouteDefinitionWriter implements RouteDefinitionRepository {
    private final RedisTemplate redisTemplate;

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            RouteDefinitionVo vo = new RouteDefinitionVo();
            BeanUtils.copyProperties(r, vo);
            log.info("保存路由信息{}", vo);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.opsForHash().put(CommonConstants.ROUTE_KEY, r.getId(), vo);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        routeId.subscribe(id -> {
            log.info("删除路由信息{}", id);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.opsForHash().delete(CommonConstants.ROUTE_KEY, id);
        });
        return Mono.empty();
    }

    /**
     * 动态路由入口
     *
     * @return
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(RouteDefinitionVo.class));
        List<RouteDefinitionVo> values = redisTemplate.opsForHash().values(CommonConstants.ROUTE_KEY);
        List<RouteDefinition> definitionList = new ArrayList<>();
        if (CollectionUtils.isEmpty(values)) {
            try {
                String url = "http://taibai-admin-biz/route/reload";
                RestTemplate restTemplate = SpringContextHolder.getBean("gatewayCommonRestTemplate");
                Map result = restTemplate.getForObject(url, Map.class);
                log.info("result={}", result);
                if (result == null) {
                    return Flux.fromIterable(definitionList);
                }
                Object codeObj = result.get("code");
                if (codeObj == null) {
                    return Flux.fromIterable(definitionList);
                }
                if (!String.valueOf(codeObj).equals("0")) {
                    return Flux.fromIterable(definitionList);
                }
            } catch (Throwable th) {
                log.error("reload route fail", th);
                return Flux.fromIterable(definitionList);
            }
        }

        values.forEach(vo -> {
            RouteDefinition routeDefinition = new RouteDefinition();
            BeanUtils.copyProperties(vo, routeDefinition);
            definitionList.add(vo);
        });
        log.debug("redis 中路由定义条数： {}， {}", definitionList.size(), definitionList);
        return Flux.fromIterable(definitionList);
    }
}
