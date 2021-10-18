package com.fitmgr.meterage.utils;

import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * 读取XML文件的工具类
 * xml对应的bean
 * @author zhangxiaokang
 * @date 2020/10/30 14:42
 */
@Data
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class FieldSetting {

    /**
     * 中文名
     */
    @XmlElement(name="fieldCnName")
    private String fieldCnName;

    /**
     * 数据库对应字段
     */
    @XmlElement(name="fieldDbName")
    private String fieldDbName;

    /**
     * 单元格宽度，0-200 不写默认自适应
     */
    @XmlElement(name="fieldWidth")
    private Integer fieldWidth;

}
