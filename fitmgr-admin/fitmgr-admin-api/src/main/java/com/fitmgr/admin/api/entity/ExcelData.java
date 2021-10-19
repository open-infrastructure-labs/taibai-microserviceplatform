package com.fitmgr.admin.api.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Fitmgr
 * @date ：Created in 2021/1/11 15:39
 * @modified By：
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExcelData implements Serializable {

    private static final long serialVersionUID = 1L;

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