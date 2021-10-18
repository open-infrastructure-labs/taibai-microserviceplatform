package com.fitmgr.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    /**
     * 待审批
     */
    ORDER_TYPE_APPROVAL_PENDING("0","待审批"),
    /**
     * 待执行
     */
    ORDER_TYPE_EXECUTE("1","待执行"),
    /**
     * 执行中
     */
    ORDER_TYPE_EXECUTING("2","执行中"),
    /**
     * 已驳回
     */
    ORDER_TYPE_DISMISSED("3","已驳回"),
    /**
     * 已完成
     */
    ORDER_TYPE_ACCOMPLISH("4","已完成"),
    /**
     * 失败
     */
   ORDER_TYPE_FAILURE("5","失败"),

    /**
     * 交付驳回
     */
    ORDER_STATUS_DELIVERY_REJECT("6","交付驳回"),

    /**
     * 执行失败，用于失败但是不最后扣减配额的失败的中间状态
     */
    ORDER_EXECUTING_FAILURE("7","执行失败");

    /**
     * 类型
     */
    private final String status;
    /**
     * 描述
     */
    private final String description;

    public static OrderStatusEnum getOrderStatus(String status){
        for(OrderStatusEnum nodeStatusEnum:OrderStatusEnum.values()){
            if(status.equals(nodeStatusEnum.getStatus())){
                return nodeStatusEnum;
            }
        }
        return  null;
    }
}
