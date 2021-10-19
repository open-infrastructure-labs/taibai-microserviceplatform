package com.fitmgr.admin.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.admin.api.entity.AllLocationTree;
import com.fitmgr.admin.api.entity.LocationTreeNode;
import com.fitmgr.admin.api.entity.Tenant;
import com.fitmgr.admin.api.entity.TenantLocationTree;
import com.fitmgr.admin.service.IAllLocationTreeService;
import com.fitmgr.admin.service.ITenantLocationTreeService;
import com.fitmgr.admin.service.ITenantService;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;
import com.fitmgr.resource.api.feign.RemoteCmdbService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncAllLocationTreeTask extends XxlBaseTaskExec {

    private IAllLocationTreeService allLocationTreeService = SpringContextHolder.getBean(IAllLocationTreeService.class);
    private ITenantLocationTreeService tenantLocationTreeService = SpringContextHolder
            .getBean(ITenantLocationTreeService.class);
    private ITenantService tenantService = SpringContextHolder.getBean(ITenantService.class);

    private RemoteCmdbService remoteCmdbService = SpringContextHolder.getBean(RemoteCmdbService.class);

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        // 调用resource服务查询全量region、AZ、RZ，然后按照层级从上到下构建全量位置树
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("componentCode", "resourcecenter_region");
        List<Map<String, Object>> regionList = remoteCmdbService.selectByCondition(map, "Y").getData();
        map.put("componentCode", "resourcecenter_availability_zone");
        List<Map<String, Object>> azList = remoteCmdbService.selectByCondition(map, "Y").getData();
        map.put("componentCode", "resourcecenter_resource_zone");
        List<Map<String, Object>> rzList = remoteCmdbService.selectByCondition(map, "Y").getData();
        // 全量位置树
        List<LocationTreeNode> locationTreeNodes = new ArrayList<LocationTreeNode>();
        // VDC位置树
        JSONArray locationTreeArray = new JSONArray();
        for (int i = 0; i < regionList.size(); i++) {
            Map<String, Object> mapre = regionList.get(i);
            LocationTreeNode locationTreeNode = new LocationTreeNode(mapre.get("uuid").toString(),
                    mapre.get("name").toString(), mapre.get("state").toString(), mapre.get("del_flag").toString(), "-1",
                    "Region", null, null);
            locationTreeNodes.add(locationTreeNode);
            JSONObject json = new JSONObject();
            json.put("uuid", mapre.get("uuid"));
            json.put("half", false);
            locationTreeArray.add(json);
        }
        for (int i = 0; i < azList.size(); i++) {
            Map<String, Object> mapaz = azList.get(i);
            LocationTreeNode locationTreeNode = new LocationTreeNode(mapaz.get("uuid").toString(),
                    mapaz.get("name").toString(), mapaz.get("state").toString(), mapaz.get("del_flag").toString(),
                    mapaz.get("region_id").toString(), "AZ", null, null);
            locationTreeNodes.add(locationTreeNode);
            JSONObject json = new JSONObject();
            json.put("uuid", mapaz.get("uuid"));
            json.put("half", false);
            locationTreeArray.add(json);
        }
        for (int i = 0; i < rzList.size(); i++) {
            Map<String, Object> maprz = rzList.get(i);
            System.out.println(maprz);
            LocationTreeNode locationTreeNode = new LocationTreeNode(maprz.get("uuid").toString(),
                    maprz.get("name").toString(), maprz.get("state").toString(), maprz.get("del_flag").toString(),
                    maprz.get("availability_zone_id") == null ? maprz.get("region_id").toString()
                            : maprz.get("availability_zone_id").toString(),
                    "RZ", maprz.get("cloud_platform_type").toString(), maprz.get("network_provider").toString());
            locationTreeNodes.add(locationTreeNode);
            JSONObject json = new JSONObject();
            json.put("uuid", maprz.get("uuid"));
            json.put("half", false);
            locationTreeArray.add(json);
        }
        String locationTree = JSON.toJSONString(locationTreeNodes);
        String treeVersion = LocalDateTime.now() + UUID.randomUUID().toString();
        List<AllLocationTree> allLocationTrees = allLocationTreeService
                .list(Wrappers.query(new AllLocationTree()).orderByDesc("id"));
        if (allLocationTrees != null && allLocationTrees.size() > 0) {
            String oldLocationTree = allLocationTrees.get(0).getLocationTree();
            if (!oldLocationTree.equals(locationTree)) {
                // 更新全量位置树
                AllLocationTree allLocationTree = new AllLocationTree();
                allLocationTree.setLocationTree(locationTree);
                allLocationTree.setTreeVersion(treeVersion);
                allLocationTreeService.save(allLocationTree);
                // 只保留两条记录
                if (allLocationTrees.size() > 1) {
                    allLocationTreeService.remove(new QueryWrapper<AllLocationTree>().lambda()
                            .lt(AllLocationTree::getId, allLocationTrees.get(0).getId() - 1));
                }
                // 获取新增位置树
                List<LocationTreeNode> oldLocationTreeJson = JSONObject.parseArray(oldLocationTree,
                        LocationTreeNode.class);
                List<LocationTreeNode> locationTreeJson = JSONObject.parseArray(locationTree, LocationTreeNode.class);

                List<String> list = new ArrayList<String>();
                List<LocationTreeNode> locationTreeJson1 = new ArrayList<LocationTreeNode>();
                locationTreeJson1.addAll(locationTreeJson);
                if (oldLocationTreeJson != null) {
                    for (LocationTreeNode locationTreeNode1 : locationTreeJson) {
                        for (LocationTreeNode locationTreeNode2 : oldLocationTreeJson) {
                            if (locationTreeNode1.getUuid().equals(locationTreeNode2.getUuid())) {
                                locationTreeJson1.remove(locationTreeNode1);
                            }
                        }
                    }
                }
                // 添加新增的节点
                for (LocationTreeNode locationTreeNode : locationTreeJson1) {
                    list.add(locationTreeNode.getUuid());
                }
                // 更新每个vdc的位置树
                List<TenantLocationTree> tenantLocationTreeList = tenantLocationTreeService
                        .list(new QueryWrapper<TenantLocationTree>().lambda());
                for (TenantLocationTree tenantLocationTree : tenantLocationTreeList) {
                    List<String> list1 = new ArrayList<String>();
                    list1.addAll(list);
                    JSONArray tenantLocationTreeArray = JSONArray.parseArray(tenantLocationTree.getLocationTree());
                    if (tenantLocationTreeArray != null) {
                        // 添加原有的位置树
                        for (Object object : tenantLocationTreeArray) {
                            JSONObject json = (JSONObject) object;
                            list1.add(json.getString("uuid"));
                        }
                    }
                    // 根据全量位置树重新构建该vdc的位置树
                    JSONArray array = new JSONArray();
                    structure(locationTreeJson, array, list1, "-1");
                    tenantLocationTree.setLocationTree(array.toJSONString());
                    tenantLocationTree.setTreeVersion(treeVersion);
                    tenantLocationTreeService.updateById(tenantLocationTree);
                }
            }
        } else {
            // 保存全量位置树
            AllLocationTree allLocationTree = new AllLocationTree();
            allLocationTree.setLocationTree(locationTree);
            allLocationTree.setTreeVersion(treeVersion);
            allLocationTreeService.save(allLocationTree);
            // 保存每个vdc的位置树
            List<Tenant> tenants = tenantService.list(new QueryWrapper<Tenant>().lambda().eq(Tenant::getDelFlag, "0"));
            TenantLocationTree tenantLocationTree = new TenantLocationTree();
            tenantLocationTree.setLocationTree(locationTreeArray.toJSONString());
            tenantLocationTree.setTreeVersion(treeVersion);
            for (Tenant tenant : tenants) {
                tenantLocationTree.setVdcId(tenant.getId());
                tenantLocationTree.setParentVdcId(tenant.getParentId());
                tenantLocationTreeService.save(tenantLocationTree);
            }
        }
        ReturnT returnt = new ReturnT();
        returnt.setCode(0);
        return returnt;
    }

    public static String structure(List<LocationTreeNode> locationTreeJson, JSONArray array, List<String> list,
            String parentId) {
        List<String> halfs = new ArrayList<String>();
        for (LocationTreeNode locationTreeNode : locationTreeJson) {
            if (locationTreeNode.getParentId().equals(parentId)) {
                String res = structure(locationTreeJson, array, list, locationTreeNode.getUuid());
                if (res != null && "half".equals(res)) {
                    // 有选有不选有半选为半选
                    JSONObject json = new JSONObject();
                    json.put("uuid", locationTreeNode.getUuid());
                    json.put("half", true);
                    array.add(json);
                    halfs.add("half");
                } else if (res != null && "in".equals(res)) {
                    // 全都选为全选
                    JSONObject json = new JSONObject();
                    json.put("uuid", locationTreeNode.getUuid());
                    json.put("half", false);
                    array.add(json);
                    halfs.add("in");
                } else if (res != null && "notin".equals(res)) {
                    // 全都不选为不选
                    halfs.add("notin");
                } else if (list.contains(locationTreeNode.getUuid())) {
                    // 不包含但之前已选为全选
                    JSONObject json = new JSONObject();
                    json.put("uuid", locationTreeNode.getUuid());
                    json.put("half", false);
                    array.add(json);
                    halfs.add("in");
                } else {
                    halfs.add("notin");
                }
            }
        }
        if ((halfs.contains("in") && halfs.contains("half") && halfs.contains("notin"))
                || (halfs.contains("in") && halfs.contains("half")) || (halfs.contains("in") && halfs.contains("notin"))
                || (halfs.contains("half") && halfs.contains("notin")) || halfs.contains("half")) {
            // 有选有不选有半选为半选
            return "half";
        } else if (halfs.contains("in")) {
            // 全都选为全选
            return "in";
        } else if (halfs.contains("notin")) {
            // 全都不选为不选
            return "notin";
        } else if (list.contains(parentId)) {
            // 不包含但之前已选为全选
            return "in";
        }
        return null;
    }

    @Override
    public void taskRollback(Task task, Exception e) {

    }
}
