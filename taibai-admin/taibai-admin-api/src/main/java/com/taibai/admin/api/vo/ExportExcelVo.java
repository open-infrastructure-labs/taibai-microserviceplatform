package com.taibai.admin.api.vo;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Taibai
 * @date ：Created in 2021/1/11 14:27
 * @modified By：
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExportExcelVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roleName;

    private String functionName;

    private String menuName;

}
