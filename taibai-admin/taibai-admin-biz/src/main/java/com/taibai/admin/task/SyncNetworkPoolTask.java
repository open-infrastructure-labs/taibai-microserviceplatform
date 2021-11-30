package com.taibai.admin.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.taibai.admin.api.constants.NetworkPoolEnum;
import com.taibai.admin.api.constants.NetworkPoolTypeEnum;
import com.taibai.admin.api.entity.AllLocationTree;
import com.taibai.admin.api.entity.LocationTreeNode;
import com.taibai.admin.api.entity.NetworkPool;
import com.taibai.admin.api.entity.NetworkPoolInfo;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.TenantNetworkPool;
import com.taibai.admin.service.IAllLocationTreeService;
import com.taibai.admin.service.INetworkPoolService;
import com.taibai.admin.service.ITenantNetworkPoolService;
import com.taibai.admin.service.ITenantService;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.job.api.core.biz.model.ReturnT;
import com.taibai.job.api.entity.Task;
import com.taibai.job.api.excutor.XxlBaseTaskExec;
import com.taibai.resource.api.feign.RemoteCmdbService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncNetworkPoolTask extends XxlBaseTaskExec {

    private final int NETWORK_POOL_SIZE = 100;

    private INetworkPoolService networkPoolService = SpringContextHolder.getBean(INetworkPoolService.class);
    private ITenantNetworkPoolService tenantNetworkPoolService = SpringContextHolder
            .getBean(ITenantNetworkPoolService.class);
    private IAllLocationTreeService allLocationTreeService = SpringContextHolder.getBean(IAllLocationTreeService.class);
    private ITenantService tenantService = SpringContextHolder.getBean(ITenantService.class);

    private RemoteCmdbService remoteCmdbService = SpringContextHolder.getBean(RemoteCmdbService.class);

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        List<AllLocationTree> allLocationTrees = allLocationTreeService
                .list(Wrappers.query(new AllLocationTree()).orderByDesc("id"));
        if (allLocationTrees != null && allLocationTrees.size() > 0) {
            // 调用resource服务查询全量网络池信息，浮动IP地址池、云主机IP地址池、VLAN池
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("componentCode", "resourcecenter_networking_sp_ip_pools");
            List<Map<String, Object>> spIpList = remoteCmdbService.selectByCondition(map, "Y").getData();
            map.put("componentCode", "resourcecenter_networking_ip_subnetpools");
            List<Map<String, Object>> ipSubnetList = remoteCmdbService.selectByCondition(map, "Y").getData();
            map.put("componentCode", "resourcecenter_networking_vlan");
            List<Map<String, Object>> vlanList = remoteCmdbService.selectByCondition(map, "Y").getData();

            syncNetworkPool(allLocationTrees.get(0), spIpList, NetworkPoolTypeEnum.SP_IP.getCode().toString());
            syncNetworkPool(allLocationTrees.get(0), ipSubnetList, NetworkPoolTypeEnum.IP_SUBNET.getCode().toString());
            syncNetworkPool(allLocationTrees.get(0), vlanList, NetworkPoolTypeEnum.VLAN.getCode().toString());
        }
        ReturnT returnt = new ReturnT();
        returnt.setCode(0);
        return returnt;
    }

    public void syncNetworkPool(AllLocationTree allLocationTree, List<Map<String, Object>> spIpList,
            String networkPoolTypeEnum) {
        NetworkPool networkPool = new NetworkPool();
        networkPool.setNetworkPoolType(networkPoolTypeEnum);
        networkPool.setVersion(allLocationTree.getTreeVersion());
        List<NetworkPoolInfo> networkPoolInfos = new ArrayList<NetworkPoolInfo>();
        for (int i = 0; i < spIpList.size(); i++) {
            Map<String, Object> map = spIpList.get(i);
            NetworkPoolInfo networkPoolInfo = new NetworkPoolInfo(map.get("uuid").toString(),
                    NetworkPoolEnum.CHOICE_ON.getCode(), map.get("resource_zone_id").toString());
            networkPoolInfos.add(networkPoolInfo);
        }
        networkPool.setNetworkPoolInfo(JSON.toJSONString(networkPoolInfos));
        List<NetworkPool> networkPools = networkPoolService
                .list(Wrappers.query(new NetworkPool()).eq("network_pool_type", networkPoolTypeEnum).orderByDesc("id"));
        if (!CollectionUtils.isEmpty(networkPools)) {
            List<NetworkPoolInfo> oldnetworkPoolInfos = JSONObject.parseArray(networkPools.get(0).getNetworkPoolInfo(),
                    NetworkPoolInfo.class);
            if (!oldnetworkPoolInfos.equals(networkPoolInfos)
                    || !allLocationTree.getTreeVersion().equals(networkPools.get(0).getVersion())) {
                networkPoolService.save(networkPool);
                // 保留100条记录
                if (networkPools.size() > NETWORK_POOL_SIZE) {
                    networkPoolService.remove(new QueryWrapper<NetworkPool>().lambda()
                            .eq(NetworkPool::getNetworkPoolType, networkPoolTypeEnum)
                            .lt(NetworkPool::getId, networkPools.get(0).getId() - 100));
                }
                // 保存每个vdc的网络池信息
                List<Tenant> tenants = tenantService
                        .list(new QueryWrapper<Tenant>().lambda().eq(Tenant::getDelFlag, "0"));
                for (Tenant tenant : tenants) {
                    List<String> uuids = new ArrayList<String>();
                    // 查询可用RZ
                    List<LocationTreeNode> locationTreeNodes = tenantService
                            .queryPoolResources(tenant.getId(), "RZ", null, null, null).getData();
                    if (locationTreeNodes != null && locationTreeNodes.size() > 0) {
                        for (LocationTreeNode locationTreeNode : locationTreeNodes) {
                            uuids.add(locationTreeNode.getUuid());
                        }
                    }
                    // 根据RZ和历史选择网络池
                    List<String> oldSpIpIds = new ArrayList<String>();
                    TenantNetworkPool tenantNetworkPool = tenantNetworkPoolService
                            .getOne(new QueryWrapper<TenantNetworkPool>().lambda()
                                    .eq(TenantNetworkPool::getVdcId, tenant.getId())
                                    .eq(TenantNetworkPool::getNetworkPoolType, networkPoolTypeEnum));
                    List<NetworkPoolInfo> oldTenantNetworkPoolInfos = JSONObject
                            .parseArray(tenantNetworkPool.getNetworkPoolInfo(), NetworkPoolInfo.class);
                    for (NetworkPoolInfo tenantNetworkPoolInfo : oldTenantNetworkPoolInfos) {
                        if (tenantNetworkPoolInfo.getChoice().equals(NetworkPoolEnum.CHOICE_OFF.getCode())) {
                            oldSpIpIds.add(tenantNetworkPoolInfo.getUuid());
                        }
                    }
                    List<NetworkPoolInfo> tenantNetworkPoolInfos = new ArrayList<NetworkPoolInfo>();
                    for (int i = 0; i < spIpList.size(); i++) {
                        Map<String, Object> map = spIpList.get(i);
                        if (uuids.contains(map.get("resource_zone_id").toString())) {
                            NetworkPoolInfo networkPoolInfo = new NetworkPoolInfo(map.get("uuid").toString(),
                                    oldSpIpIds.contains(map.get("uuid").toString())
                                            ? NetworkPoolEnum.CHOICE_OFF.getCode()
                                            : NetworkPoolEnum.CHOICE_ON.getCode(),
                                    map.get("resource_zone_id").toString());
                            tenantNetworkPoolInfos.add(networkPoolInfo);
                        }
                    }
                    tenantNetworkPool.setVersion(allLocationTree.getTreeVersion());
                    tenantNetworkPool.setNetworkPoolInfo(JSON.toJSONString(tenantNetworkPoolInfos));
                    tenantNetworkPoolService.updateById(tenantNetworkPool);
                }
            }
        } else {
            networkPoolService.save(networkPool);
            // 保存每个vdc的网络池信息
            List<Tenant> tenants = tenantService.list(new QueryWrapper<Tenant>().lambda().eq(Tenant::getDelFlag, "0"));
            for (Tenant tenant : tenants) {
                List<String> uuids = new ArrayList<String>();
                // 查询可用RZ
                List<LocationTreeNode> locationTreeNodes = tenantService
                        .queryPoolResources(tenant.getId(), "RZ", null, null, null).getData();
                if (locationTreeNodes != null && locationTreeNodes.size() > 0) {
                    for (LocationTreeNode locationTreeNode : locationTreeNodes) {
                        uuids.add(locationTreeNode.getUuid());
                    }
                }
                // 根据RZ全选网络池
                List<NetworkPoolInfo> tenantNetworkPoolInfos = new ArrayList<NetworkPoolInfo>();
                for (int i = 0; i < spIpList.size(); i++) {
                    Map<String, Object> map = spIpList.get(i);
                    if (uuids.contains(map.get("resource_zone_id").toString())) {
                        NetworkPoolInfo networkPoolInfo = new NetworkPoolInfo(map.get("uuid").toString(),
                                NetworkPoolEnum.CHOICE_ON.getCode(), map.get("resource_zone_id").toString());
                        tenantNetworkPoolInfos.add(networkPoolInfo);
                    }
                }
                TenantNetworkPool tenantNetworkPool = new TenantNetworkPool();
                tenantNetworkPool.setNetworkPoolType(networkPoolTypeEnum);
                tenantNetworkPool.setVdcId(tenant.getId());
                tenantNetworkPool.setVersion(allLocationTree.getTreeVersion());
                tenantNetworkPool.setNetworkPoolInfo(JSON.toJSONString(tenantNetworkPoolInfos));
                tenantNetworkPoolService.save(tenantNetworkPool);
            }
        }
    }

    @Override
    public void taskRollback(Task task, Exception e) {

    }
}
