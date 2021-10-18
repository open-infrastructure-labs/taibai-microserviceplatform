package com.fitmgr.meterage.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.common.core.constant.enums.DeleteFlagStatusEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.MeterageProjectPropertyDTO;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.mapper.MeterageProjectMapper;
import com.fitmgr.meterage.mapper.MeterageProjectPropertyMapper;
import com.fitmgr.meterage.service.IMeterageProjectPropertyService;
import com.fitmgr.resource.api.feign.RemoteDictionaryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
@Slf4j
@Service
@AllArgsConstructor
public class MeterageProjectPropertyServiceImpl extends ServiceImpl<MeterageProjectPropertyMapper, MeterageProjectProperty> implements IMeterageProjectPropertyService {

    private final MeterageProjectPropertyMapper meterageProjectPropertyMapper;

    private final MeterageProjectMapper meterageProjectMapper;

    private final RemoteDictionaryService remoteDictionaryService;

    @Override
    public List<MeterageProjectProperty> selectByCondition(Map<String, Object> map) {
        return meterageProjectPropertyMapper.selectByMap(map);
    }

    @Override
    public List<MeterageProjectProperty> selectByComponentCode(String componentCode) {
        return meterageProjectPropertyMapper.selectHeaderByComponentCode(componentCode);
    }

    @Override
    public List<MeterageProjectProperty> selectFilterForChargeItem(MeterageProjectPropertyDTO meterageProjectPropertyDTO) {
        List<MeterageProjectProperty> projects = meterageProjectPropertyMapper.selectList(Wrappers.<MeterageProjectProperty>lambdaQuery()
                .eq(MeterageProjectProperty::getMeterageProjectId, meterageProjectPropertyDTO.getMeterageProjectId())
                .eq(MeterageProjectProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus()));
        // 过滤并组装规格
        Boolean flag = false;
        Iterator<MeterageProjectProperty> iterator = projects.iterator();
        while (iterator.hasNext()) {
            MeterageProjectProperty meterageProjectProperty = iterator.next();
            if ("flavor_id".equals(meterageProjectProperty.getSourceKey())) {
                flag = true;
                iterator.remove();
            }
            // 处理foreignKey
            if (StringUtils.isBlank(meterageProjectProperty.getForeignKey())) {
                meterageProjectProperty.setForeignKey(meterageProjectProperty.getSourceKey());
            }
        }
        if (flag) {
            // 改计量项属性为虚拟云主机的，添加一个虚拟的规格属性
            MeterageProjectProperty meterageProjectProperty = new MeterageProjectProperty();
            // 设置虚拟id
            Integer max = Collections.max(projects.stream().map(meterageProjectProperty1 -> meterageProjectProperty1.getId()).collect(Collectors.toList()));
            meterageProjectProperty.setId(max + 10);
            meterageProjectProperty.setSourceKey("flavor_id");
            meterageProjectProperty.setKeyName("云主机规格");
            projects.add(meterageProjectProperty);
        }
        return projects;
    }

    @Override
    public JSONArray getChargeItemProperties(List<MeterageProjectProperty> meterageProjectProperties, Integer meterageProjectId) {
        // 查询component
        LambdaQueryWrapper<MeterageProject> queryWrapper = Wrappers.<MeterageProject>lambdaQuery().eq(MeterageProject::getId, meterageProjectId);
        MeterageProject meterageProject = meterageProjectMapper.selectOne(queryWrapper);
        if (null == meterageProject) {
            throw new BusinessException("计量项配置为空！");
        }
        String componentCode = meterageProject.getComponentCode();
        // 查询数据字典过滤计量项属性，取交集作为计费项属性
        R propertiesResult = remoteDictionaryService.getTreeByTypeId(ChargeConstant.CHARGE_ITEM_PROPERTIES_UUID);
        log.info("数据字典查询计费属性配置：{}", JSONObject.toJSONString(propertiesResult));
        if (null == propertiesResult || null == propertiesResult.getData() || propertiesResult.getCode() != 0) {
            return new JSONArray();
        }
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(propertiesResult.getData()));
        JSONObject accountConfigJsonObj = jsonObject.getJSONObject(ChargeConstant.ACCOUNT_CONFIG);
        Set<String> keys = accountConfigJsonObj.keySet();
        if (!keys.contains(componentCode)) {
            return new JSONArray();
        }
        JSONObject componentCodeJson = accountConfigJsonObj.getJSONObject(componentCode);
        JSONObject keyJson = componentCodeJson.getJSONObject(ChargeConstant.KEY);
        if (null == keyJson || keyJson.size() == 0) {
            return new JSONArray();
        }
        Map<String, MeterageProjectProperty> meterageProjectPropertyMap = meterageProjectProperties.stream().collect(Collectors.toMap(MeterageProjectProperty::getSourceKey, (p) -> p));
        // 循环获取数据字典中配置的属性及属性值
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, Object> stringObjectEntry : keyJson.entrySet()) {
            if (null == meterageProjectPropertyMap.get(stringObjectEntry.getKey())) {
                continue;
            }
            JSONObject jsonObj = JSONObject.parseObject(JSONObject.toJSONString(meterageProjectPropertyMap.get(stringObjectEntry.getKey())));
            JSONObject jsonValue = JSONObject.parseObject(JSONObject.toJSONString(stringObjectEntry.getValue()));
            JSONArray arrayValue = jsonValue.getJSONArray(ChargeConstant.VALUE);
            JSONObject valueJsonObj = JSONObject.parseObject(JSONObject.toJSONString(arrayValue.get(0)));
            String dicValue = valueJsonObj.getString(ChargeConstant.DIC_VALUE);
            jsonObj.put(ChargeConstant.DIC_VALUE, dicValue);
            jsonArray.add(jsonObj);
        }
        return jsonArray;
    }
}
