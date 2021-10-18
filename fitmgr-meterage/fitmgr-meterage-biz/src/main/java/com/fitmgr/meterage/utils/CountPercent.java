package com.fitmgr.meterage.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * 计算百分比
 * @author zhangxiaokang
 * @date 2020/11/5 16:47
 */
public class CountPercent {

    public static String numFormat(BigDecimal price,BigDecimal totalPrice){
        //转换成浮点型
        float numeratorf = price.floatValue();
        float denominatorf = totalPrice.floatValue();
        //获取百分数实例
        NumberFormat nt = NumberFormat.getPercentInstance();
        nt.setMinimumFractionDigits(2);
        //得到结果
        return nt.format(numeratorf/denominatorf);
    }
}
