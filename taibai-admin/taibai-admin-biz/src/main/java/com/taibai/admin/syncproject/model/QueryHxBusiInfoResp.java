package com.taibai.admin.syncproject.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QueryHxBusiInfoResp implements Serializable {

    private static final long serialVersionUID = 5007699485413427067L;

    private List<HxBusiInfo> detail;
}
