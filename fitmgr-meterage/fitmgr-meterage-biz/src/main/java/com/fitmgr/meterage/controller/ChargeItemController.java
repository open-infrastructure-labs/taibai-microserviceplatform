package com.fitmgr.meterage.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.service.IMeterageChargeItemService;
import com.fitmgr.meterage.validate.ChargeItemValidate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 计费项-前端控制器
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/charge/item")
public class ChargeItemController {

    private final IMeterageChargeItemService chargeItemService;

    @PostMapping
    public R createChargeItem(@RequestBody ChargeItemDTO chargeItemDTO) {
        // 入参校验
        R chargeItemValudate = ChargeItemValidate.createChargeItemValudate(chargeItemDTO);
        if (chargeItemValudate.getCode() != R.ok().getCode()) {
            return R.failed(chargeItemValudate.getMsg());
        }
        // 入库及匹配计量记录
        if (chargeItemService.createChargeItem(chargeItemDTO)) {
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.CREATE_CHARGE_ITEM_ERROR);
    }

    @GetMapping("/{chargeItemId}")
    public R getChargeItemDetail(@PathVariable("chargeItemId") String chargeItemId) {
        return R.ok(chargeItemService.findChargeItemVOByUuid(chargeItemId));
    }

    @GetMapping("/components/{resourceId}")
    public R getResourceList(@PathVariable("resourceId") Integer resourceId) {
        return R.ok(chargeItemService.getComponentListByCondition(resourceId));
    }

    @PutMapping
    public R updateChargeItem(@RequestBody ChargeItemDTO chargeItemDTO) {
        // 入参校验
        R chargeItemValudate = ChargeItemValidate.createChargeItemValudate(chargeItemDTO);
        if (chargeItemValudate.getCode() != R.ok().getCode()) {
            return R.failed(chargeItemValudate.getMsg());
        }
        if (chargeItemService.updateChargeItem(chargeItemDTO)) {
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.UPDATE_CHARGE_ITEM_ERROR);
    }

    @PutMapping("/enable/{chargeItemId}")
    public R enableChargeItem(@PathVariable("chargeItemId") String chargeItemId) {
        if (chargeItemService.enableChargeItem(chargeItemId)) {
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.ENABLE_CHARGE_ITEM_ERROR);
    }

    @PutMapping("/disable/{chargeItemId}")
    public R disableChargeItem(@PathVariable("chargeItemId") String chargeItemId) {
        if (chargeItemService.disableChargeItem(chargeItemId)) {
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.DISABLE_CHARGE_ITEM_ERROR);
    }

    @DeleteMapping("/{chargeItemId}")
    public R deleteChargeItem(@PathVariable("chargeItemId") String chargeItemId) {
        if (chargeItemService.deleteChargeItem(chargeItemId)) {
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.DELETE_CHARGE_ITEM_ERROR);
    }

    @PostMapping("/list/page")
    public R selectPage(Page page, @RequestBody ChargeItemDTO chargeItemDTO) {
        return R.ok(chargeItemService.selectPage(page, chargeItemDTO));
    }

    @PostMapping("/list/export")
    public void exportChargeItem(HttpServletResponse response, Page page, @RequestBody ChargeItemDTO chargeItemDTO) {
        if (StringUtils.isBlank(chargeItemDTO.getXmlTemplateName())) {
            throw new BusinessException(BusinessEnum.CHARGEITEM_EXPORT_TEMPLATE_ERROR);
        }
        chargeItemService.exportExcel(response, page, chargeItemDTO);
    }
}
