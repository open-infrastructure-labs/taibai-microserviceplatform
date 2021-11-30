
package com.taibai.common.gateway.vo;

import java.io.Serializable;

import org.springframework.cloud.gateway.route.RouteDefinition;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Taibai
 * @date 2018/10/31
 *       <p>
 *       扩展此类支持序列化a See RouteDefinition.class
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RouteDefinitionVo extends RouteDefinition implements Serializable {
    /**
     * 路由名称
     */
    private String routeName;
}
