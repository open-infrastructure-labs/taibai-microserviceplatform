package com.taibai.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.taibai.admin.api.entity.NetworkPool;
import com.taibai.admin.service.INetworkPoolService;
import com.taibai.common.core.util.R;
import com.taibai.job.api.entity.Task;
import com.taibai.job.api.sdk.FhJobApiController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 全量网络池信息 前端控制器
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/networkPool")
@Api(value = "allLocationTree", tags = "全量网络池信息模块")
public class NetworkPoolController {

    private final INetworkPoolService networkPoolService;

    /**
     * 查询全量网络池信息
     *
     * @param id 租户id
     * @return R
     */
    @GetMapping
    @ApiOperation(value = "查询全量网络池信息")
    public R<NetworkPool> networkPoolInfo() {
        List<NetworkPool> networkPools = networkPoolService.list(Wrappers.query(new NetworkPool()).orderByDesc("id"));
        return R.ok(networkPools.get(0));
    }

    /**
     * 更新全量网络池信息
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @PutMapping
    @ApiOperation(value = "更新全量网络池信息")
    public R updateNetworkPool() {
        Task task = FhJobApiController.queryByTaskId("SyncNetworkPoolTask");
        FhJobApiController.trigger(task);
        return R.ok();
    }
}
