package com.taibai.common.core.util;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * @Auther: DZL
 * @Date: 2019/11/25
 * @Description: 集合对象复制工具类
 */

public class ObjectCopyUtils {
    /**
     * 从List<A> copy到List<B>
     *
     * @param list
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> copy(List<?> list, Class<T> clazz) {
        String oldOb = JSON.toJSONString(list);
        return JSON.parseArray(oldOb, clazz);
    }

    /**
     * 从对象A copy到 对象B
     *
     * @param ob    A
     * @param clazz B.class
     * @return B
     */
    public static <T> T copy(Object ob, Class<T> clazz) {
        String oldOb = JSON.toJSONString(ob);
        return JSON.parseObject(oldOb, clazz);
    }

}
