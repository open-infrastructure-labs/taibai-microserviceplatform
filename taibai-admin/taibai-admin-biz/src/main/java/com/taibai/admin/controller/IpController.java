package com.fitmgr.admin.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.constants.IpTypeEnum;
import com.fitmgr.admin.api.entity.Ip;
import com.fitmgr.admin.service.IIpService;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.security.util.IPUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/ip")
@Api(value = "ip", tags = "ip白名单")
public class IpController {

    private final String COMMA = ",";

    private IIpService ipService;

    /**
     * 
     * 根据id查询平台信息
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id查询ip白名单信息")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "id", dataType = "Long", required = true, value = "id"))
    @GetMapping("/getById")
    public R info(@RequestParam(name = "id") Long id) {
        try {
            Ip ip = ipService.getById(id);
            if (ip == null) {
                log.error("根据id[{}]查询IP白名单为空", id);
                return R.failed(Boolean.FALSE, String.format("IP白名单为空"));
            }
            return R.ok(ip);
        } catch (Exception e) {
            log.error("根据id[{}]查询IP白名单异常", id, e);
            return R.failed("id不能为空");
        }
    }

    /**
     * 查询IP白名单列表
     */
    @ApiOperation(value = "查询IP白名单列表")
    @GetMapping("/list")
    public R list() {
        try {
            List<Ip> ips = ipService.list();
            return R.ok(ips);
        } catch (Exception e) {
            log.error("查询IP白名单列表异常", e);
            return R.failed("查询IP白名单列表失败");
        }
    }

    /**
     * 查询IP白名单列表
     */
    @ApiOperation(value = "查询启用状态的IP白名单列表")
    @GetMapping("/useList")
    public R useList() {
        try {
            List<Ip> ips = ipService.list(Wrappers.<Ip>query().lambda().eq(Ip::getStatus, 0));
            return R.ok(ips);
        } catch (Exception e) {
            log.error("查询启用状态IP白名单列表异常", e);
            return R.failed("查询启用状态IP白名单列表失败");
        }
    }

    /**
     * 分页查询注册平台列表
     */
    @ApiOperation(value = "分页查询IP白名单列表")
    @GetMapping("/page")
    public R page(Page page, Ip ip) {
        try {
            LambdaQueryWrapper<Ip> chargeItemLambdaQueryWrapper = Wrappers.<Ip>lambdaQuery()
                    .like(StringUtils.isNotBlank(ip.getName()), Ip::getName, ip.getName())
                    .orderByDesc(Ip::getCreateTime);
            return R.ok(ipService.page(page, chargeItemLambdaQueryWrapper));
        } catch (Exception e) {
            log.error("分页查询IP白名单列表，ip={}", ip, e);
            return R.failed(new ArrayList<>());
        }
    }

    /**
     * 
     * 新增IP白名单
     *
     * @param platform
     * @return
     */
    @ApiOperation(value = "新增IP白名单")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "body", name = "ip", dataType = "Ip", required = true, value = "新增IP白名单"))
    @PostMapping("/add")
    public R add(@RequestBody Ip ip) {
        try {
            Integer ipType = ip.getType();
            if (ipType == IpTypeEnum.IP.getCode()) {
                for (String i : ip.getIps().split(",")) {
                    if (!IPUtils.isIP(i) || !verifyIp(i)) {
                        return R.failed("[" + i + "]不符合IP格式");
                    }
                }
            } else if (ipType == IpTypeEnum.RANGE.getCode() && IPUtils.getIpFormartLong(ip.getIpStart()) > IPUtils.getIpFormartLong(ip.getIpEnd())) {
                log.error("起始IP[{}]不能大于结束IP[{}]", ip.getIpStart(), ip.getIpEnd());
                return R.failed("起始IP不能大于结束IP");
            } else if (ipType == IpTypeEnum.CIDR.getCode() && !IPUtils.isCidr(ip.getCidr())) {
                log.error("IP[{}]不符合CIDR格式", ip.getCidr());
                return R.failed("IP不符合CIDR格式"); 
            }
            // 创建时默认启用
            ip.setStatus(0);
            ip.setCreateTime(new Date());
            return R.ok(ipService.save(ip));
        } catch (Exception e) {
            log.error("新增IP白名单异常，ip={}", ip, e);
            return R.failed("新增失败，请稍后重试");
        }
    }

    /**
     * 
     * 更新IP白名单
     *
     * @param platform
     * @return
     */
    @ApiOperation(value = "更新IP白名单")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "body", name = "ip", dataType = "Ip", required = true, value = "IP白名单"))
    @PostMapping("/update")
    public R update(@RequestBody Ip ip) {
        try {
            Integer ipType = ip.getType();
            if (ipType == IpTypeEnum.IP.getCode()) {
                for (String i : ip.getIps().split(",")) {
                    if (!IPUtils.isIP(i) || !verifyIp(i)) {
                        return R.failed("[" + i + "]不符合IP格式或为不可用IP");
                    }
                }
            } else if (ipType == IpTypeEnum.RANGE.getCode() && IPUtils.getIpFormartLong(ip.getIpStart()) > IPUtils.getIpFormartLong(ip.getIpEnd())) {
                log.error("起始IP[{}]不能大于结束IP[{}]", ip.getIpStart(), ip.getIpEnd());
                return R.failed("起始IP不能大于结束IP");
            } else if (ipType == IpTypeEnum.CIDR.getCode() && !IPUtils.isCidr(ip.getCidr())) {
                log.error("IP[{}]不符合CIDR格式", ip.getCidr());
                return R.failed("IP不符合CIDR格式"); 
            }
            ip.setUpdateTime(new Date());
            return R.ok(ipService.updateById(ip));
        } catch (Exception e) {
            log.error("更新IP白名单异常，ip={}", ip, e);
            return R.failed("更新失败，请稍后重试");
        }
    }

    /**
     * 
     * 删除IP白名单
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "删除IP白名单")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "id", dataType = "Long", required = true, value = "id"))
    @DeleteMapping("/delete/{id}")
    public R update(@PathVariable(name = "id") Long id) {
        try {
            return R.ok(ipService.removeById(id));
        } catch (Exception e) {
            log.error("根据id[{}]删除IP白名单异常", id, e);
            return R.failed("删除失败，请稍后重试");
        }
    }

    /**
     * 
     * 更改IP白名单状态
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "更改状态")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "id", dataType = "Long", required = true, value = "id"))
    @PostMapping("/status")
    public R status(@RequestParam(name = "id", required = true) Long id,
            @RequestParam(name = "status", required = true) Integer status) {
        try {
            Ip ip = ipService.getById(id);
            if (ip == null) {
                log.error("根据id[{}]查询IP白名单为空", id);
                return R.failed(Boolean.FALSE, String.format("IP白名单为空"));
            }
            ip.setStatus(status);
            ip.setUpdateTime(new Date());
            return R.ok(ipService.updateById(ip));
        } catch (Exception e) {
            log.error("更改IP白名单状态失败，id={}", id, e);
            return R.failed("更改状态失败，请稍后重试");
        }
    }
    
    /**
     * 
     * 校验ip格式的最后一位是否在1~254之间
     *
     * @param ip
     * @return
     */
    private boolean verifyIp(String ip) {
        return !ip.endsWith(".0") && !ip.endsWith(".255");
    }
}
