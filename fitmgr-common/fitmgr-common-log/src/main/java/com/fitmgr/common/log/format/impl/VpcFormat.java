package com.fitmgr.common.log.format.impl;

import java.util.Iterator;
import java.util.Map;

import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.common.log.format.IFormat;
import com.fitmgr.resource.api.feign.RemoteCmdbService;
import com.fitmgr.resource.api.feign.RemoteComponentService;
import com.fitmgr.resource.api.feign.RemoteResourceTypeService;
import com.fitmgr.resource.api.vo.ComponentVO;
import com.fitmgr.resource.api.vo.ResourceTypeVO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2020-05-03
 */
@Slf4j
public class VpcFormat implements IFormat {

    @SneakyThrows
    @Override
    public String format(String json) {
        // 因为是通过反射调用并没有交给Spring管理
        // 所以要用手工注入的方式
        // 以后如果要调资源管理的接口也是一样
        RemoteComponentService remoteComponentService = SpringContextHolder.getBean(RemoteComponentService.class);
        RemoteResourceTypeService remoteResourceTypeService = SpringContextHolder
                .getBean(RemoteResourceTypeService.class);
        RemoteCmdbService remoteCmdbService = SpringContextHolder.getBean(RemoteCmdbService.class);

        log.info("json:" + json);

        // 转换json数组
        JsonArray jsonArray = new JsonParser().parse(json).getAsJsonArray();

        // 获取迭代器
        Iterator<JsonElement> iterator = jsonArray.iterator();

        // 操作code
        String componentCode = null;

        // serviceId
        String serverId = null;
        // 遍历
        while (iterator.hasNext()) {
            JsonElement next = iterator.next();
            componentCode = next.getAsJsonObject().get("componentCode").getAsString();
            // 批量操作标识
            JsonElement jsonElement = next.getAsJsonObject().get("isBatchOperate");
            if (jsonElement != null) {
                Boolean isBatchOperate = jsonElement.getAsBoolean();
                if (isBatchOperate) {
                    // 批量操作
                    JsonArray batchOperateArray = next.getAsJsonObject().get("batchOperateParam").getAsJsonArray();

                    Iterator<JsonElement> batchIterator = batchOperateArray.iterator();

                    // 个数
                    int count = 0;
                    while (batchIterator.hasNext()) {
                        batchIterator.next();
                        count++;
                    }

                    // 构建资源日志
                    String format = String.format(" 数量: %s", count);
                    log.info("批量个数format: {}", format);
                    return format;
                }
            }

            JsonArray operateParamsArray = next.getAsJsonObject().get("operateParams").getAsJsonArray();

            // 获取迭代器
            Iterator<JsonElement> serverIterator = operateParamsArray.iterator();

            // 遍历
            while (serverIterator.hasNext()) {
                serverId = serverIterator.next().getAsJsonObject().get("server_id").getAsString();
            }

        }

        // 调用资源管理接口
        R<ComponentVO> result = remoteComponentService.selectComponentByCode(componentCode);

        // 调用feign失败
        if (CommonConstants.SUCCESS != result.getCode()) {
            return BusinessEnum.RESOURCE_COMPONENT_NOT_EXIST.getDescription();
        }

        // 获取组件对象
        ComponentVO componentVO = result.getData();

        log.info("componentVO: " + componentVO);

        R<ResourceTypeVO> resourceType = remoteResourceTypeService.get(componentVO.getResourceId());
        // 调用feign失败
        if (CommonConstants.SUCCESS != resourceType.getCode()) {
            return BusinessEnum.RESOURCETYPE_NULL.getDescription();
        }

        // 组件资源类型
        ResourceTypeVO resourceTypeVO = resourceType.getData();

        R<Map<String, Object>> resultInstance = remoteCmdbService.selectOneByInstanceId(componentCode, serverId);
        // 调用feign失败
        if (CommonConstants.SUCCESS != resourceType.getCode()) {
            return BusinessEnum.RESOURCETYPE_NULL.getDescription();
        }

        Map<String, Object> cmdbMap = resultInstance.getData();
        log.info("cmdb data: {}", cmdbMap);

        // 实例名
        String cmpInstanceName = (String) cmdbMap.get("cmpInstanceName");
        // 别名
        String alias = (String) cmdbMap.get("alias");

        // 资源池
        String resourcePoolIdName = (String) cmdbMap.get("resource_pool_idName");

        // 子网
        String subnetIdName = (String) cmdbMap.get("subnet_idName");

        // 构建资源日志
        String format = String.format(" 云资源类型: %s , 版本: %s ," + "实例名: %s, alias别名: %s, 资源池名称: %s, 子网:%s, ",
                resourceTypeVO.getResourceTypeName(), componentVO.getVersion(), cmpInstanceName, alias,
                resourcePoolIdName, subnetIdName);
        return format;
    }
}
