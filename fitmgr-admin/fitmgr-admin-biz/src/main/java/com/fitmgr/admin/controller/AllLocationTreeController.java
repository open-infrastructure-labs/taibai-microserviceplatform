package com.fitmgr.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.admin.api.entity.AllLocationTree;
import com.fitmgr.admin.api.entity.LocationTreeNode;
import com.fitmgr.admin.service.IAllLocationTreeService;
import com.fitmgr.common.core.util.R;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.sdk.FhJobApiController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 全量位置树表 前端控制器
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/allLocationTree")
@Api(value = "allLocationTree", tags = "全量位置树模块")
public class AllLocationTreeController {

    private final static String TENANT_ID = "-1";

    private final IAllLocationTreeService allLocationTreeService;

    /**
     * 查询全量位置树
     *
     * @param id 租户id
     * @return R
     */
    @GetMapping
    @ApiOperation(value = "查询全量位置树")
    public R<List<LocationTreeNode>> allLocationTreeInfo() {
        // 全量位置树
        List<LocationTreeNode> locationTreeNodes = new ArrayList<LocationTreeNode>();
        AllLocationTree allLocationTree = allLocationTreeService
                .list(Wrappers.query(new AllLocationTree()).orderByDesc("id")).get(0);
        List<LocationTreeNode> locationTreeJson = JSONObject.parseArray(allLocationTree.getLocationTree(),
                LocationTreeNode.class);
        for (LocationTreeNode locationTreeNode : locationTreeJson) {
            if (TENANT_ID.equals(locationTreeNode.getParentId())) {
                structure(locationTreeNode, locationTreeJson);
                locationTreeNodes.add(locationTreeNode);
            }
        }
        return R.ok(locationTreeNodes);
    }

    public void structure(LocationTreeNode parentLocationTreeNode, List<LocationTreeNode> locationTreeJson) {
        for (LocationTreeNode locationTreeNode : locationTreeJson) {
            if (locationTreeNode.getParentId().equals(parentLocationTreeNode.getUuid())) {
                structure(locationTreeNode, locationTreeJson);
                List<LocationTreeNode> childs = parentLocationTreeNode.getChilds() == null
                        ? new ArrayList<LocationTreeNode>()
                        : parentLocationTreeNode.getChilds();
                childs.add(locationTreeNode);
                parentLocationTreeNode.setChilds(childs);
            }
        }
    }

    /**
     * 更新位置树信息
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @PutMapping
    @ApiOperation(value = "更新全量位置树信息")
    public R updateAllLocationTree() {
        Task task = FhJobApiController.queryByTaskId("SyncAllLocationTreeTask");
        FhJobApiController.trigger(task);
        return R.ok();
    }
}
