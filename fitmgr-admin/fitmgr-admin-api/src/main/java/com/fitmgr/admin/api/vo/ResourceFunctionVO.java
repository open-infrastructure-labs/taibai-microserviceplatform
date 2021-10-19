package com.fitmgr.admin.api.vo;

import java.io.Serializable;
import java.util.List;

import com.fitmgr.admin.api.entity.Function;
import com.fitmgr.admin.api.entity.ResourceMenu;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 资源操作VO
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceFunctionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源菜单
     */
    @ApiModelProperty(value = "资源菜单", name = "resourceMenu", required = false)
    private ResourceMenu resourceMenu;

    /**
     * 操作list
     */
    @ApiModelProperty(value = "操作list", name = "functions", required = false)
    private List<Function> functions;

}
