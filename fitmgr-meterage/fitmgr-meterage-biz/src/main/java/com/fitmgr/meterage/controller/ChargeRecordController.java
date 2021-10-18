package com.fitmgr.meterage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.ResourceChargeRecordDTO;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 账单记录Controller
 *
 * @author zhangxiaokang
 * @date 2020/10/30 10:10
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/charge/record")
@Api(value = "chargeRecord", tags = "账单费用记录")
public class ChargeRecordController {

    private final IResourceChargeRecordService resourceChargeRecordService;

    @PostMapping("/list/page")
    @ApiOperation(value = "获取账单记录列表")
    public R selectPage(Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.selectPage(page, resourceChargeRecordDTO));
    }

    @PostMapping("/list/export")
    @ApiOperation(value = "账单记录导出")
    public void exportChargeItem(HttpServletResponse response, Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        if (StringUtils.isBlank(resourceChargeRecordDTO.getXmlTemplateName())) {
            throw new BusinessException(BusinessEnum.CHARGE_BILL_EXPORT_TEMPLATE_ERROR);
        }
        resourceChargeRecordService.exportChargeBillDetail(response, page, resourceChargeRecordDTO);
    }

    @PostMapping("/tenant/month")
    @ApiOperation(value = "租户账单月度费用统计")
    public R totalTenantMonth(Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.totalTenantMonth(page, resourceChargeRecordDTO));
    }

    @PostMapping("/tenant/year")
    @ApiOperation(value = "租户账单半年或一年费用统计")
    public R totalTenantYear(Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.totalTenantYear(page, resourceChargeRecordDTO));
    }

    @PostMapping("/project/month")
    @ApiOperation(value = "Project账单月度费用统计")
    public R totalProjectMonth(Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.totalProjectMonth(page, resourceChargeRecordDTO));
    }

    @PostMapping("/project/year")
    @ApiOperation(value = "Project账单半年或一年费用统计")
    public R totalProjectYear(Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.totalProjectYear(page, resourceChargeRecordDTO));
    }

    @GetMapping("/totoal/charge")
    @ApiOperation(value = "全部费用及整体分布")
    public R totalTenantPrice() {
        return R.ok(resourceChargeRecordService.totalTenantPrice());
    }

    @PostMapping("/review/tenant/price")
    @ApiOperation(value = "近半年或一年某个租户费用趋势")
    public R reviewTenantPrice(Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.reviewTenantPrice(page, resourceChargeRecordDTO));
    }

    @PostMapping("/review/project/price")
    @ApiOperation(value = "近半年或一年某个Project费用趋势")
    public R reviewProjectPrice(Page page, @RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.reviewProjectPrice(page, resourceChargeRecordDTO));
    }

    @PostMapping("/total/price")
    @ApiOperation(value = "某个月某个租户的费用占比")
    public R reviewTotalPrice(@RequestBody ResourceChargeRecordDTO resourceChargeRecordDTO) {
        return R.ok(resourceChargeRecordService.reviewTotalPrice(resourceChargeRecordDTO));
    }

    @PostMapping("/resource/delete")
    @ApiOperation(value = "资源下线，资源计费")
    public R deleteResourceBillDetail(@RequestBody ResourceMeterageRecord resourceMeterageRecord) {
        return R.ok(resourceChargeRecordService.deleteResourceBillDetail(resourceMeterageRecord));
    }

    @PostMapping("/resource/add")
    @ApiOperation(value = "新增资源计费记录")
    public R saveResourceBillDetail(@RequestBody ResourceMeterageRecord resourceMeterageRecord) {
        return R.ok(resourceChargeRecordService.saveResourceBillDetail(resourceMeterageRecord));
    }

    @PostMapping("/resource/update")
    @ApiOperation(value = "更新资源计费记录")
    public R updateResourceBillDetail(@RequestBody ResourceMeterageRecord resourceMeterageRecord) {
        return R.ok(resourceChargeRecordService.updateResourceBillDetail(resourceMeterageRecord));
    }
}
