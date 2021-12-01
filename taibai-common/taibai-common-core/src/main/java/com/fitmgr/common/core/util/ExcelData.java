package com.taibai.common.core.util;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * excel 数据
 * 
 * @author Taibai
 * @date 2019-11-28
 */
@Data
public class ExcelData implements Serializable {
    /**
     * 表头
     */
    private List<String> titles;

    /**
     * 数据
     */
    private List<List<Object>> rows;

    /**
     * 页签名称
     */
    private String name;
}
