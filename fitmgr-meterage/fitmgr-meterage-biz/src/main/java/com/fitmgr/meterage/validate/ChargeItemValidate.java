package com.fitmgr.meterage.validate;

import com.fitmgr.common.core.util.R;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.dto.ChargeItemPropertyDTO;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.mapper.MeterageProjectMapper;
import com.fitmgr.resource.api.feign.RemoteResourceTypeService;
import com.fitmgr.resource.api.vo.ResourceTypeVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zhangxiaokang
 * @date 2020/10/31 15:21
 */
public class ChargeItemValidate {

    public static R createChargeItemValudate(ChargeItemDTO chargeItemDTO) {

        MeterageProjectMapper projectMapper = SpringContextHolder.getBean(MeterageProjectMapper.class);

        RemoteResourceTypeService resourceTypeService = SpringContextHolder.getBean(RemoteResourceTypeService.class);

        if (StringUtils.isBlank(chargeItemDTO.getChargeName())) {
            return R.failed("计费项名称为空！");
        }
        if (null == chargeItemDTO.getCloudPlatformId()) {
            return R.failed("云平台为空！");
        }
        R<ResourceTypeVO> resourceTypeVOR = resourceTypeService.get(chargeItemDTO.getCloudPlatformId());
        ResourceTypeVO resourceTypeVO = resourceTypeVOR.getData();
        if (null == resourceTypeVO) {
            return R.failed("云平台不存在");
        }
        if (null == chargeItemDTO.getMeterageItemId()) {
            return R.failed("计量项为空！");
        }
        MeterageProject meterageProject = projectMapper.selectById(chargeItemDTO.getMeterageItemId());
        if (meterageProject == null) {
            return R.failed("计量项不存在");
        }
        if (StringUtils.isBlank(chargeItemDTO.getComponentCode())) {
            return R.failed("源组件CODE为空！");
        }
        if (null == chargeItemDTO.getChargeFlavorUnit() || null == chargeItemDTO.getChargeFlavorTime()) {
            return R.failed("计费单位为空！");
        }
        if (null == chargeItemDTO.getPrice()) {
            return R.failed("折前单价为空！");
        }
        if (chargeItemDTO.getPrice().compareTo(new BigDecimal("0.00")) == -1) {
            return R.failed("折前单价不能小于0！");
        }
        if (null == chargeItemDTO.getChargeStatus()) {
            return R.failed("计费状态为空！");
        }
        List<ChargeItemPropertyDTO> chargeItemPropertyDTOS = chargeItemDTO.getChargeItemPropertyDTOS();
        if(chargeItemPropertyDTOS != null){
            for (ChargeItemPropertyDTO chargeItemPropertyDTO : chargeItemPropertyDTOS) {
                if (StringUtils.isBlank(chargeItemPropertyDTO.getChargePropertyKey())) {
                    return R.failed("计费属性KEY为空！");
                }
                if (StringUtils.isBlank(chargeItemPropertyDTO.getChargePropertyValue())) {
                    return R.failed("计费属性VALUE为空！");
                }
            }
        }
        return R.ok();
    }

}
