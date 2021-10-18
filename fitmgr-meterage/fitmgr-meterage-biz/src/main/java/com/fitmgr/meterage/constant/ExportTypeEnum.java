package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导出类型枚举
 *
 * @author zhangxiaokang
 * @date 2020/10/30 17:33
 */
@Getter
@AllArgsConstructor
public enum ExportTypeEnum {

    CHARGE_ITEM_EXPORT("ChargeItem", "计费项列表"),
    DISCOUNT_ITEM_EXPORT("DiscountItem", "折扣项列表"),
    CHARGE_RECORDS_EXPORT("ChargeRecords", "资源账单记录"),
    ;
    private String templateCode;
    private String templateName;

    public static String getTemplateName(String templateCode) {
        for (ExportTypeEnum value : ExportTypeEnum.values()) {
            if (value.getTemplateCode().equals(templateCode)) {
                return value.getTemplateName();
            }
        }
        return null;
    }
}
