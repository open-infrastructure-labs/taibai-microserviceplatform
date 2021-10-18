package com.fitmgr.meterage.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.dto.DiscountItemDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.service.IDiscountItemService;
import com.fitmgr.meterage.service.IMeterageChargeItemService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 折扣项-前端控制器
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/discount/item")
public class DiscountItemController {

    private final IDiscountItemService discountItemService;

    private final IMeterageChargeItemService chargeItemService;

    @PostMapping
    public R createChargeItem(@RequestBody DiscountItemDTO discountItemDTO) {
        discountItemService.createDiscountItem(discountItemDTO);
        return R.ok();
    }

    @PutMapping
    public R updateChargeItem(@RequestBody DiscountItemDTO discountItemDTO) {
        discountItemService.updateDiscountItem(discountItemDTO);
        return R.ok();
    }

    @DeleteMapping("/{discountItemId}")
    public R deleteChargeItem(@PathVariable("discountItemId") String discountItemId) {
        discountItemService.deleteDiscountItem(discountItemId);
        return R.ok();
    }

    @PostMapping("/list/page")
    public R selectPage(Page page, @RequestBody DiscountItemDTO discountItemDTO) {
        return R.ok(discountItemService.selectPage(page, discountItemDTO));
    }

    @GetMapping("/get/{disCountId}")
    public R selectPage(@PathVariable String disCountId) {
        return R.ok(discountItemService.getDisCountDetail(disCountId));
    }

    /**
     * 查询已经在使用的计费项列表
     * @param chargeItemDTO
     * @return
     */
    @PostMapping("/list")
    public R selectList(@RequestBody ChargeItemDTO chargeItemDTO) {
        List<ChargeItem> chargeItemList =  chargeItemService.list(Wrappers.<ChargeItem>lambdaQuery().eq(ChargeItem::getDelFlag, 0));
        List<ChargeItem> chargeItems = new ArrayList<>();
        for (ChargeItem chargeItem:chargeItemList) {
            if(chargeItem.getPlanExecuteTime() == null || LocalDateTime.now().isAfter(chargeItem.getPlanExecuteTime())){
                chargeItems.add(chargeItem);
            }
        }
        return R.ok(chargeItems);
    }

    @PostMapping("/list/export")
    public void exportChargeItem(HttpServletResponse response, Page page, @RequestBody DiscountItemDTO discountItemDTO) {
        if (StringUtils.isBlank(discountItemDTO.getXmlTemplateName())) {
            throw new BusinessException(BusinessEnum.DISCOUNT_ITEM_EXPORT_TEMPLATE_ERROR);
        }
        discountItemService.exportExcel(response, page, discountItemDTO);
    }
}
