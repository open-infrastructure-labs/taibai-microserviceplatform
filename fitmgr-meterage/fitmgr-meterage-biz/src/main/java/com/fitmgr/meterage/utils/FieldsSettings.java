package com.fitmgr.meterage.utils;

import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * 读取XML文件的工具类
 * xml对应的bean
 * @author zhangxiaokang
 * @date 2020/10/30 14:43
 */
@Data
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FieldsSettings")
public class FieldsSettings {

    @XmlElementWrapper(name="Settings")
    @XmlElement(name = "fieldSetting")
    private List<FieldSetting> fieldSetting;

}
