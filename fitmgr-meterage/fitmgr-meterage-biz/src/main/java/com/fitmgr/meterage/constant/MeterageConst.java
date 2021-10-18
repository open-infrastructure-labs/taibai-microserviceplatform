package com.fitmgr.meterage.constant;

/**
 * @ClassName MeterageConst
 * @Description TODO
 * @Author BDWang
 * @Date 2021/8/11 10:44
 **/
public interface MeterageConst {

    /**
     * 高可用的mq队列名
     **/
    String METE_HA_QUEUE_NAME = "queue_meterage_ha";

    /**
     * 高可用的mq交换机
     **/
    String METE_HA_EX_NAME = "meterage_exchange";
}
