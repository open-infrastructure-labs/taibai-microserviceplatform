package com.fitmgr.meterage.api.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author jy
 * @version 1.0
 * @date 2021/1/12 14:11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceMeterageRecordMapVO {

    private List<Map<String, String>> maps;

    private long total=0;

    private long size=10;

    private long current=1L;
}
