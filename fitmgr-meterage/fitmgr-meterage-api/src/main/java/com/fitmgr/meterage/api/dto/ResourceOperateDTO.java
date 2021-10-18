package com.fitmgr.meterage.api.dto;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fitmgr.log.api.entity.OperateLog;
import com.fitmgr.resource.api.entity.APIParam;
import com.fitmgr.resource.api.entity.Component;
import com.fitmgr.resource.api.entity.ServiceOperat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author jy
 * @version 1.0
 * @date 2021/1/12 10:26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceOperateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**d
     * 资源类型ID
     */
    @ApiModelProperty(value = "资源类型code,操作所在的云")
    private String resourceTypeCode;

    /**
     * 组件code
     */
    @ApiModelProperty(value = "资源组件唯一识别名,也就是操作注册所在的组件的code")
    @NotNull(message = "资源组件code不能为空")
    private String componentCode;

    /**
     * 操作码
     */
    @ApiModelProperty(value = "操作码")
    @NotNull(message = "操作code不能为空")
    private String operateCode;

    /**
     * 前端传递的操作参数
     */
    @ApiModelProperty(value = "操作参数")
    private JSONArray operateParams;

    /**
     * 非批量操作的操作参数
     */
    @ApiModelProperty(value = "操作参数")
    private JSONObject operateParam;

    /**
     * 资源实例自定义名
     */
    @ApiModelProperty(value = "资源实例自定义名,操作是从从列表中哪一行发起的，就是那一行的cmpInstanceName")
    private String cmpInstanceName ;

    /**
     * 组件对象
     */
    @ApiModelProperty(value = "组件对象")
    private Component component;

    /**
     * 操作对象
     */
    @ApiModelProperty(value = "操作对象")
    private ServiceOperat serviceOperat;

    /**
     * cmdb中的资源实例
     */
    @ApiModelProperty(value = "cmdb中的资源实例")
    private Map<String,Object> map;

    /**
     * 批量操作参数
     */
    @ApiModelProperty(value = "批量操作参数")
    private JSONArray batchOperateParam;

    /**
     * 批量操作参数
     */
    @ApiModelProperty(value = "批量操作标识  true-批量   false null -非批量操作")
    private Boolean isBatchOperate;

    /**
     * 封装好的api参数由operateParam转换而来
     */
    private List<APIParam> apiParamList;

    @ApiModelProperty(value = "操作日志ID")
    private Long operateLogId;

    @ApiModelProperty(value = "用户ID")
    private Integer cmpUserId;

    @ApiModelProperty(value = "project的ID")
    private Integer cmpProjectId;

    @ApiModelProperty(value = "租户ID")
    private Integer cmpTenantId;

    @ApiModelProperty(value = "订单详情")
    private String orderDetail;
}
