
package com.taibai.admin.controller;

import java.net.URI;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taibai.admin.service.SysRouteConfService;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.util.R;
import com.taibai.common.gateway.vo.RouteDefinitionVo;
import com.taibai.common.log.annotation.SysLog;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 路由
 *
 * @author Taibai
 * @date 2018-11-06 10:17:18
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/route")
@Api(value = "route", tags = "动态路由管理模块")
public class SysRouteConfController {
    private final SysRouteConfService sysRouteConfService;

    private final RedisTemplate redisTemplate;

    /**
     * 获取当前定义的路由信息
     *
     * @return
     */
    @GetMapping
    @ApiOperation(value = "获取当前定义的路由信息", notes = "获取当前定义的路由信息")
    public R listRoutes() {
        return new R<>(sysRouteConfService.list());
    }

    /**
     * 修改路由
     *
     * @param routes 路由定义
     * @return
     */
    @SysLog("修改路由")
    @PutMapping
    @ApiOperation(value = "修改路由", notes = "修改路由")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "body", name = "routes", dataType = "JSONArray", required = true, value = "routes"))
    public R updateRoutes(@RequestBody JSONArray routes) {
        return new R(sysRouteConfService.updateRoutes(routes));
    }

    @GetMapping(value = { "/reload" })
    public R reloadRoutes() {
        Boolean result = redisTemplate.delete(CommonConstants.ROUTE_KEY);
        log.info("删除网关路由 {} ", result);
        try {
            sysRouteConfService.routes().forEach(route -> {
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
                    redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(RouteDefinitionVo.class));
                    redisTemplate.opsForHash().put(CommonConstants.ROUTE_KEY, route.getRouteId(), vo);
                }
            });
        } catch (Throwable th) {
            log.error("config route info fail", th);
            return R.failed("config route info fail");
        }
        return R.ok();
    }

}
