
package com.fitmgr.admin.api.vo;

import lombok.Data;

/**
 * @author Fitmgr
 * @date 2018/8/27 前端日志vo
 */
@Data
public class PreLogVo {
    private String url;
    private String time;
    private String user;
    private String type;
    private String message;
    private String stack;
    private String info;
}
