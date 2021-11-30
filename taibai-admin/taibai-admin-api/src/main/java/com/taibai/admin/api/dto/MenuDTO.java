package com.taibai.admin.api.dto;

import com.taibai.admin.api.entity.Menu;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Auther: DZL
 * @Date: 2019/11/25
 * @Description: 菜单dto
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuDTO extends Menu {
    /**
     * 租户id集合
     */
    @ApiModelProperty(name = "tenantIds", value = "租户id集合", required = false)
    private List<Integer> tenantIds;
}
