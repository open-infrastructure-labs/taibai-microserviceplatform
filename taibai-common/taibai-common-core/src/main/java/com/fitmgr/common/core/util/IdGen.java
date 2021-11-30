package com.taibai.common.core.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 封装各种生成唯一性ID算法的工具类.
 *
 * @author Taibai
 * @date 2018年5月21日 上午9:40:53
 */
public class IdGen {

    private static SecureRandom random = new SecureRandom();

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割.
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 使用SecureRandom随机生成int.
     */
    public static int randomLong() {
        return Math.abs(random.nextInt(Integer.MAX_VALUE));
    }

    /**
     * 雪花算法，生成18位Long类型的id
     *
     * @return
     */
    public static long snowflakeIdWorker() {
        return SnowflakeIdWorker.generateId();
    }
}
