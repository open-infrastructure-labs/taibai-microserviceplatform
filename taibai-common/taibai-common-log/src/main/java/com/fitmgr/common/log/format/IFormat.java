package com.taibai.common.log.format;

/**
 * 为了资源的传参格式化
 * 
 * @author Taibai
 * @date 2020-05-03
 */
public interface IFormat {
    /**
     * json数组格式化 需要拓展自行添加实现类
     * 
     * @param json json
     * @return 字符串
     */
    String format(String json);
}
