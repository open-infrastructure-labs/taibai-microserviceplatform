package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资源名称展示枚举
 *
 * @author zhangxiaokang
 * @date 2020/11/6 17:00
 */
@Getter
@AllArgsConstructor
public enum ResourceViewNameConvertEnum {

    COMPUTER_INSTANCE("resourcecenter_compute_instance_v1", "name", "云主机"),
    BLOCKSTORAGE_VOLUME("resourcecenter_blockstorage_volume_v1", "volume_name", "云硬盘"),
    RESOURCECENTER_FW_FIRWALL("resourcecenter_fw_firewall_v1", "name", null),
    RESOURCECENTER_LB_LOADBALANCER("resourcecenter_lb_loadbalancer_v1", "name", "负载均衡"),
    RESOURCECNETER_NETWORKING_VPC("resourcecenter_networking_vpc_v1", "name", "vpc"),
    RESOURCECENTER_OBJECTSTORAGE_BUCKET("resourcecenter_objectstorage_bucket_v1", "bucket_name", "对象存储"),
    OTHERS_RESOURCE_ITEM("resourcecenter_others_resource_v1","name","其他费用"),
    ;

    private String componentCode;
    private String componentNameKey;
    private String percentName;

    /**
     * 根据componentCode获取展示name的key
     *
     * @param componentCode
     * @return
     */
    public static String getNameKey(String componentCode) {
        for (ResourceViewNameConvertEnum value : ResourceViewNameConvertEnum.values()) {
            if (value.getComponentCode().equals(componentCode)) {
                return value.getComponentNameKey();
            }
        }
        return null;
    }

    public static String getPercentNameKey(String componentCode) {
        for (ResourceViewNameConvertEnum value : ResourceViewNameConvertEnum.values()) {
            if (componentCode.equals(value.getComponentCode())) {
                return value.getPercentName();
            }
        }
        return null;
    }
}
