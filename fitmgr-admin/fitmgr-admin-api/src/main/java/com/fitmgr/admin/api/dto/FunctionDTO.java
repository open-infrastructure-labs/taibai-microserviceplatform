package com.fitmgr.admin.api.dto;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fitmgr.admin.api.entity.Function;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @Auther: DZL
 * @Date: 2019/11/25
 * @Description: 功能dto
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FunctionDTO extends Function {
    @ApiModelProperty(value = "功能状态", name = "status", required = false)
    private String status;

    @ApiModelProperty(value = "功能范围", name = "operatingRange", required = false)
    private String operatingRange;

    /**
     *  VDC级别范围
     */
    @ApiModelProperty(value = "VDC级别范围", name = "tenantRange", required = false)
    private String tenantRange;
}
