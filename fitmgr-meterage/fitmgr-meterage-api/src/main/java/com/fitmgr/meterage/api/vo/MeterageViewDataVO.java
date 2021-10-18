package com.fitmgr.meterage.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author jy
 * @version 1.0
 * @date 2021/4/9 16:19
 */
@Data
public class MeterageViewDataVO implements Serializable {

    private List<Integer> vdcIds;

    private Map<String, String> property;
}
