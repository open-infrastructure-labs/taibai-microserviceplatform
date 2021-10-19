package com.fitmgr.admin.api.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Fitmgr
 * @date ：Created in 2021/1/13 14:27
 * @modified By：
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExcelLevelMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;

    private String parentTitle;

    private String menuId;

    private String parentId;
}
