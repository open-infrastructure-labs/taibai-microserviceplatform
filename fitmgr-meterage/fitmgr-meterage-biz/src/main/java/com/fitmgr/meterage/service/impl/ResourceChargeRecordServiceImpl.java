package com.fitmgr.meterage.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.dto.TenantDTO;
import com.fitmgr.admin.api.entity.AuthCheck;
import com.fitmgr.admin.api.entity.Tenant;
import com.fitmgr.admin.api.feign.RemoteAuthService;
import com.fitmgr.admin.api.feign.RemoteProjectService;
import com.fitmgr.admin.api.feign.RemoteTenantService;
import com.fitmgr.admin.api.vo.ProjectVO;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.constant.enums.DeleteFlagStatusEnum;
import com.fitmgr.common.core.constant.enums.EnableStatusEnum;
import com.fitmgr.common.core.constant.enums.OperatingRangeEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.feign.XxlTaskService;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.dto.ChargeItemPropertyDTO;
import com.fitmgr.meterage.api.dto.ResourceChargeRecordDTO;
import com.fitmgr.meterage.api.entity.*;
import com.fitmgr.meterage.api.vo.ChargeItemVO;
import com.fitmgr.meterage.api.vo.DiscountItemVO;
import com.fitmgr.meterage.api.vo.ResourceChargeRecordVO;
import com.fitmgr.meterage.concurrent.AsynDeleteResourceTaskThread;
import com.fitmgr.meterage.concurrent.MonthTotalChargeBillThread;
import com.fitmgr.meterage.constant.*;
import com.fitmgr.meterage.mapper.*;
import com.fitmgr.meterage.service.IMeterageChargeItemService;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import com.fitmgr.meterage.utils.*;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-22
 */
@Slf4j
@Service
@AllArgsConstructor
public class ResourceChargeRecordServiceImpl extends ServiceImpl<ResourceChargeRecordMapper, ResourceChargeRecord> implements IResourceChargeRecordService {

    /**
     * 每批次处理的数据量
     */
    private static final Integer SIZE = 1000;

    private static final String INITIALIZATION = "0.00";

    private final ChargeItemMapper chargeItemMapper;

    private final RemoteTenantService remoteTenantService;

    private final RemoteAuthService remoteAuthService;

    private final RemoteProjectService remoteProjectService;

    private final DiscountItemMapper discountItemMapper;

    private final MeterageProjectMapper meterageProjectMapper;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final ChargeItemPropertyMapper chargeItemPropertyMapper;

    private final ResourceChargeRecordMapper resourceChargeRecordMapper;

    @Override
    public boolean saveResourceBillDetail(ResourceMeterageRecord resourceMeterageRecord) {
        log.info("resourceMeterageRecord = {}", JSONObject.toJSONString(resourceMeterageRecord));
        String componentCode = resourceMeterageRecord.getComponentCode();
        if (StringUtils.isBlank(componentCode)) {
            log.info("componentCode is null!");
            return true;
        }
        MeterageProject meterageProject = this.getMeterageProject(componentCode);
        if (null == meterageProject) {
            log.info("meterageProject is null!");
            return true;
        }
        resourceMeterageRecord.setMeterageId(meterageProject.getId());
        LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getMeterageItemId, resourceMeterageRecord.getMeterageId())
                .eq(ChargeItem::getChargeStatus, EnableStatusEnum.ENABLE.getStatus())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());

        // 根据计量项ID获取该计量项对应的计费项列表
        List<ChargeItem> chargeItems = chargeItemMapper.selectList(queryWrapper);
        log.info("chargeItems = {}", JSONObject.toJSONString(chargeItems));
        if (CollectionUtils.isEmpty(chargeItems)) {
            // 该计量项下无任何计费项，直接返回，该资源不入账单明细库
            log.info("该计量项下无任何计费项，直接返回，该资源不入账单明细库！");
            return true;
        }

        // 获取符合计量项的所有计费项的所有计费属性
        List<String> chargeItemList = chargeItems.stream().map(chargeItem -> chargeItem.getUuid()).collect(Collectors.toList());
        LambdaQueryWrapper<ChargeItemProperty> propertyLambdaQueryWrapper = Wrappers.<ChargeItemProperty>lambdaQuery()
                .in(ChargeItemProperty::getChargeUuid, chargeItemList)
                .eq(ChargeItemProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());

        // 查询计费属性列表
        List<ChargeItemProperty> chargeItemPropertyList = chargeItemPropertyMapper.selectList(propertyLambdaQueryWrapper);
        log.info("chargeItemPropertyList={}", JSONObject.toJSONString(chargeItemPropertyList));

        // 转换计费属性List为Map集合，KEY为计费项目的UUID，VALUE为该计费项属性的List集合
        Map<String, List<ChargeItemProperty>> mapParam = new ConcurrentHashMap<>();
        for (ChargeItemProperty chargeItemProperty : chargeItemPropertyList) {
            if (null == mapParam.get(chargeItemProperty.getChargeUuid())) {
                List<ChargeItemProperty> list = new ArrayList<>();
                list.add(chargeItemProperty);
                mapParam.put(chargeItemProperty.getChargeUuid(), list);
            } else {
                List<ChargeItemProperty> chargeItemPropertyList1 = mapParam.get(chargeItemProperty.getChargeUuid());
                chargeItemPropertyList1.add(chargeItemProperty);
                mapParam.put(chargeItemProperty.getChargeUuid(), chargeItemPropertyList1);
            }
        }
        // 获取符合计费属性及属性值的计费项
        List<ChargeItemDTO> chargeItemDTOList = this.getChargeItemAndChargeProperties(resourceMeterageRecord, chargeItems, mapParam);
        log.info("符合条件的计费项为：chargeItemDTOList = {}", JSONObject.toJSONString(chargeItemDTOList));
        if (CollectionUtils.isEmpty(chargeItemDTOList)) {
            log.info("无符合条件的计费属性");
            return true;
        }
        // 当有多个符合条件的计费项时，匹配规则优先级规则为：按照过滤条件最多的计费项计费，当计费项的计费属性一样多的时候，默认匹配到合适的就退出，后面不再进行匹配了
        ChargeItemDTO chargeItemDTO = this.getChargeItemDTO(chargeItemDTOList);
        log.info("匹配出唯一计费项为：chargeItemDTO = {}", JSONObject.toJSONString(chargeItemDTO));
        if (null == chargeItemDTO) {
            log.info("未匹配到合适的计费项!");
            return true;
        }
        // 获取折扣项列表
        List<DiscountItemVO> disCountItemList = new ArrayList<>();
        LambdaQueryWrapper<DiscountItem> discountItemLambdaQueryWrapper = Wrappers.<DiscountItem>lambdaQuery()
                .eq(chargeItemDTO.getUuid() != null, DiscountItem::getChargeId, chargeItemDTO.getUuid())
                .eq(DiscountItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .eq(DiscountItem::getDiscountStatus, EnableStatusEnum.ENABLE.getStatus());
        List<DiscountItem> discountItems = discountItemMapper.selectList(discountItemLambdaQueryWrapper);
        log.info("discountItems = {}", JSONObject.toJSONString(discountItems));
        discountItems.forEach(discountItem -> {
            DiscountItemVO discountItemVO = new DiscountItemVO();
            BeanUtils.copyProperties(discountItem, discountItemVO);
            disCountItemList.add(discountItemVO);
        });
        ResourceChargeRecord resourceChargeRecord = getChargeBillDetail(resourceMeterageRecord, disCountItemList, chargeItemDTO);
        resourceChargeRecordMapper.insert(resourceChargeRecord);
        log.info("resource={},入库计费记录表完成！", JSONObject.toJSONString(resourceMeterageRecord));
        return true;
    }

    private MeterageProject getMeterageProject(String componentCode) {
        LambdaQueryWrapper<MeterageProject> meterageProjectLambdaQueryWrapper = Wrappers.<MeterageProject>lambdaQuery()
                .eq(MeterageProject::getComponentCode, componentCode)
                .eq(MeterageProject::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        return meterageProjectMapper.selectOne(meterageProjectLambdaQueryWrapper);
    }

    /**
     * 当计费项有多个的时候，匹配一个合适的计费项
     * @param chargeItemDTOList
     * @return
     */
    private ChargeItemDTO getChargeItemDTO(List<ChargeItemDTO> chargeItemDTOList) {
        if (chargeItemDTOList.size() == 1) {
            return chargeItemDTOList.get(0);
        }
        List<Integer> propertiesCount = new ArrayList<>();
        chargeItemDTOList.forEach(chargeItem -> propertiesCount.add(chargeItem.getChargeItemPropertyDTOS().size()));
        Integer maxSize = Collections.max(propertiesCount);
        for (ChargeItemDTO itemDTO : chargeItemDTOList) {
            if (maxSize.equals(itemDTO.getChargeItemPropertyDTOS().size())) {
                return itemDTO;
            }
        }
        return null;
    }

    /**
     * 获取完全匹配计费属性的计费项
     * @param resourceMeterageRecord
     * @param chargeItems
     * @param mapParam
     * @return
     */
    private List<ChargeItemDTO> getChargeItemAndChargeProperties(ResourceMeterageRecord resourceMeterageRecord, List<ChargeItem> chargeItems, Map<String, List<ChargeItemProperty>> mapParam) {
        List<ChargeItemDTO> chargeItemDTOList = new ArrayList<>();
        if (mapParam.size() > 0) {
            for (Map.Entry<String, List<ChargeItemProperty>> stringListEntry : mapParam.entrySet()) {
                // 匹配计费属性
                List<ChargeItemProperty> chargeItemProperties = stringListEntry.getValue();
                // 具体某个计费项目对应的计费属性的KEY和VALUE
                Map<String, String> chargeProperMap = new ConcurrentHashMap<>();
                chargeItemProperties.forEach(chargeItemProperty -> {
                    chargeProperMap.put(chargeItemProperty.getChargePropertyKey(), chargeItemProperty.getChargePropertyValue());
                });
                // 获取资源的属性及数据
                JSONObject jsonObject = JSONObject.parseObject(resourceMeterageRecord.getData());
                Object flavorParam = jsonObject.get(ChargeConstant.FLAVOR_ID);
                // 获取某个计量项下面的的每一个计量属性及属性值，并解析为map形式
                Map<String, String> resourceMapParam = new ConcurrentHashMap<>();
                if (flavorParam != null) {
                    // 计量项为虚拟云主机，需要对规格属性进行拆分
                    JSONObject flavorJson = JSONObject.parseObject(JSONObject.toJSONString(flavorParam));
                    for (Map.Entry<String, Object> stringObjectEntry : flavorJson.entrySet()) {
                        resourceMapParam.put(stringObjectEntry.getKey(), stringObjectEntry.getValue().toString());
                    }
                    // 如果存在flavor_id的可以，需要先把flavor_id的key及value剔除掉，因为上面已经对flavor_id进行了解析，并将key及value存在了map当中
                    jsonObject.remove(ChargeConstant.FLAVOR_ID);
                }
                for (Map.Entry<String, Object> stringObjectEntry : jsonObject.entrySet()) {
                    if (null == stringObjectEntry.getValue()) {
                        continue;
                    }
                    resourceMapParam.put(stringObjectEntry.getKey(), stringObjectEntry.getValue().toString());
                }
                // 资源的计量项属性KEY是否完全包含计费项属性KEY
                if (!resourceMapParam.keySet().containsAll(chargeProperMap.keySet())) {
                    // KEY不完全匹配
                    log.info("该计费属性KEY不完全匹配");
                    continue;
                }
                // 资源的KEY及VALUE是否完全与配置的计费项属性相等
                boolean notPropertyFlag = false;
                for (Map.Entry<String, String> stringEntry : chargeProperMap.entrySet()) {
                    String entryKey = stringEntry.getKey();
                    String entryValue = stringEntry.getValue();
                    String dbValue = resourceMapParam.get(entryKey);
                    if (!entryValue.equals(dbValue)) {
                        notPropertyFlag = true;
                        break;
                    }
                }
                if (notPropertyFlag) {
                    // VALUE值不相等
                    log.info("该计费属性KEY相等，VLAUE不相等");
                    continue;
                }

                // 获取该资源的计费项，并转换为计费项目DTO
                ChargeItemDTO chargeItemDTO = new ChargeItemDTO();
                String chargeItemUuid = stringListEntry.getKey();
                for (ChargeItem chargeItem : chargeItems) {
                    if (chargeItem.getUuid().equals(chargeItemUuid)) {
                        BeanUtils.copyProperties(chargeItem, chargeItemDTO);
                    }
                }
                log.info("符合条件的计费项：chargeItemDTO={}", JSONObject.toJSONString(chargeItemDTO));
                log.info("符合条件的计费项属性：chargeItemProperties={}", JSONObject.toJSONString(chargeItemProperties));
                if (CollectionUtils.isNotEmpty(chargeItemProperties)) {
                    List<ChargeItemPropertyDTO> chargeItemPropertyDTOS = new ArrayList<>();
                    for (ChargeItemProperty chargeItemProperty : chargeItemProperties) {
                        ChargeItemPropertyDTO chargeItemPropertyDTO = new ChargeItemPropertyDTO();
                        BeanUtils.copyProperties(chargeItemProperty, chargeItemPropertyDTO);
                        chargeItemPropertyDTOS.add(chargeItemPropertyDTO);
                    }
                    chargeItemDTO.setChargeItemPropertyDTOS(chargeItemPropertyDTOS);
                }
                // 将符合条件的计费项存起来
                chargeItemDTOList.add(chargeItemDTO);
            }
        } else {
            // 获取该资源的计费项，并转换为计费项目DTO
            ChargeItemDTO chargeItemDTO = new ChargeItemDTO();
            ChargeItem chargeItem = chargeItems.get(0);
            BeanUtils.copyProperties(chargeItem, chargeItemDTO);
            log.info("符合条件的计费项：chargeItemDTO={}", JSONObject.toJSONString(chargeItemDTO));
            // 将符合条件的计费项存起来
            chargeItemDTOList.add(chargeItemDTO);
        }

        return chargeItemDTOList;
    }

    @Override
    public boolean deleteResourceBillDetail(ResourceMeterageRecord resourceMeterageRecord) {

        // 需要下线的资源需要查询任务中心有没有待执行需要入库的资源，如果有则需要删除任务
        AsynDeleteResourceTaskThread asynDeleteResourceTaskThread = new AsynDeleteResourceTaskThread(resourceMeterageRecord.getCmpInstanceName());
        threadPoolTaskExecutor.execute(asynDeleteResourceTaskThread);

        // 查询校验是否有符合条件的账单记录，并保存在chargeBillDetailList中
        List<ResourceChargeRecord> resourceChargeRecordList = validateChargeBillDetail(resourceMeterageRecord);
        if (CollectionUtils.isEmpty(resourceChargeRecordList)) {
            return true;
        }

        // 设置资源下线备注
        String remark = ChargeConstant.RESOURCE_OFFINE;
        this.counterCharge(resourceChargeRecordList, remark);
        return true;
    }

    @Override
    public boolean updateResourceBillDetail(ResourceMeterageRecord resourceMeterageRecord) {
        // 查询校验是否有符合条件的账单记录，并保存在chargeBillDetailList中
        String cmpInstanceName = resourceMeterageRecord.getCmpInstanceName();
        LambdaQueryWrapper<ResourceChargeRecord> queryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getCmpInstanceName, cmpInstanceName)
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .isNull(ResourceChargeRecord::getFinishUseTime)
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ResourceChargeRecord> resourceChargeRecords = resourceChargeRecordMapper.selectList(queryWrapper);
        List<ResourceChargeRecord> updateResourceChargeRecords = resourceChargeRecords.stream().filter(resourceChargeRecord -> null == resourceChargeRecord.getFinishUseTime()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(updateResourceChargeRecords)) {
            log.info("无资源计费记录需要更新！");
            return true;
        }
        if (updateResourceChargeRecords.size() > 1) {
            throw new BusinessException(BusinessEnum.CHARGE_BILL_REPETITION);
        }
        // 停止原有计费记录
        String remark = ChargeConstant.RESOURCE_ITEM_CHANGE;
        this.counterCharge(updateResourceChargeRecords, remark);
        return true;
    }

    private List<ResourceChargeRecord> validateChargeBillDetail(ResourceMeterageRecord resourceMeterageRecord) {
        String cmpInstanceName = resourceMeterageRecord.getCmpInstanceName();
        LambdaQueryWrapper<ResourceChargeRecord> queryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getCmpInstanceName, cmpInstanceName)
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ResourceChargeRecord> resourceChargeRecords = resourceChargeRecordMapper.selectList(queryWrapper);
        List<ResourceChargeRecord> resourceChargeRecordList = new ArrayList<>();
        if (CollectionUtils.isEmpty(resourceChargeRecords)) {
            log.info("查不到计费状态的资源记录！");
            return resourceChargeRecordList;
        }
        for (ResourceChargeRecord resourceChargeRecord : resourceChargeRecords) {
            if (resourceChargeRecord.getResourceOffFlag().equals(1)) {
                log.info("服务已下线/删除，不进行再次下线/删除计费!");
                continue;
            }
            if (resourceChargeRecord.getEnableFlag().equals(1)) {
                // 服务暂时处于禁用状态的，不进行费用的再次结算计费，但是要把resourceOffFlag改为1，表示为服务下线
                resourceChargeRecord.setResourceOffFlag(1);
                resourceChargeRecordMapper.updateById(resourceChargeRecord);
                continue;
            }
            if (resourceChargeRecord.getFinishUseTime() != null && resourceChargeRecord.getEnableFlag().equals(0)) {
                // 月度的结算或者计费项/折扣项更新的结算费用,不需要再次进行下线，但是要把resourceOffFlag改为1，表示为服务下线
                resourceChargeRecord.setResourceOffFlag(1);
                resourceChargeRecordMapper.updateById(resourceChargeRecord);
                continue;
            }
            // 1-表示资源需下线
            resourceChargeRecord.setResourceOffFlag(1);
            resourceChargeRecordList.add(resourceChargeRecord);
        }
        if (resourceChargeRecordList.size() > 1) {
            // 该资源出现了两条结束时间都为NULL的记录，重复计费了
            throw new BusinessException(BusinessEnum.CHARGE_BILL_REPETITION);
        }
        return resourceChargeRecordList;
    }

    @Override
    public void counterCharge(List<ResourceChargeRecord> resourceChargeRecordList, String remark) {
        for (ResourceChargeRecord resourceChargeRecord : resourceChargeRecordList) {
            // 查询计费项
            LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                    .eq(ChargeItem::getUuid, resourceChargeRecord.getChargeId());
            ChargeItem chargeItem = chargeItemMapper.selectOne(queryWrapper);
            if (null == chargeItem) {
                log.info("====== 计费项为空或者已被禁用！！！！！======");
                continue;
            }

            // 根据当前截至时间，计算时长
            resourceChargeRecord.setFinishUseTime(LocalDateTime.now());

            // 设置更新时间
            resourceChargeRecord.setUpdateTime(LocalDateTime.now());

            // 设置时长（N个单位时长）
            this.setDuration(resourceChargeRecord, chargeItem);

            // 计算总费用
            BigDecimal totalCharge = new BigDecimal(String.valueOf(resourceChargeRecord.getChargeUsage()))
                    .multiply(new BigDecimal(String.valueOf(resourceChargeRecord.getDuration())))
                    .multiply(resourceChargeRecord.getPrice())
                    .multiply(resourceChargeRecord.getDiscount())
                    .multiply(new BigDecimal("0.1"));
            resourceChargeRecord.setTotalCharge(totalCharge);

            // 设置计费截至时间，需要计算到本次计费周期结束的时间
            LocalDateTime beginUseTime = resourceChargeRecord.getBeginUseTime();
            LocalDateTime updateTime = resourceChargeRecord.getUpdateTime();
            Long updateCount = this.getTime(chargeItem, beginUseTime, updateTime);

            // 设置计费截至时间
            LocalDateTime taskDateTime = DateTimeConvertUtil.calculateDateTime(beginUseTime, updateCount + 1, chargeItem.getChargeFlavorTime());
            resourceChargeRecord.setFinishUseTime(taskDateTime);

            // 设置备注
            resourceChargeRecord.setRemark(remark);
            resourceChargeRecordMapper.updateById(resourceChargeRecord);
        }
    }

    private void setDuration(ResourceChargeRecord resourceChargeRecord, ChargeItem chargeItem) {
        // 计算时长
        Integer timeUnit = chargeItem.getChargeFlavorTime();
        switch (timeUnit) {
            case 1:
                // 时数差
                resourceChargeRecord.setDuration(DateTimeConvertUtil.hourDiff(resourceChargeRecord.getBeginUseTime(), resourceChargeRecord.getFinishUseTime()));
                break;
            case 2:
                // 天数差
                resourceChargeRecord.setDuration(DateTimeConvertUtil.dayDiff(resourceChargeRecord.getBeginUseTime(), resourceChargeRecord.getFinishUseTime()));
                break;
            case 3:
                // 月数差
                resourceChargeRecord.setDuration(DateTimeConvertUtil.monthDiff(resourceChargeRecord.getBeginUseTime(), resourceChargeRecord.getFinishUseTime()));
                break;
            case 5:
                // 年数差
                resourceChargeRecord.setDuration(DateTimeConvertUtil.yearDiff(resourceChargeRecord.getBeginUseTime(), resourceChargeRecord.getFinishUseTime()));
                break;
            default:
                throw new BusinessException(BusinessEnum.TIME_UNIT_ERROR);
        }
    }

    @Override
    public ResourceChargeRecord getChargeBillDetail(ResourceMeterageRecord resourceMeterageRecord, List<DiscountItemVO> disCountItemList, ChargeItemDTO chargeItemDTO) {
        ResourceChargeRecord resourceChargeRecord = new ResourceChargeRecord();
        resourceChargeRecord.setUuid(UUID.randomUUID().toString());
        resourceChargeRecord.setCmpInstanceName(resourceMeterageRecord.getCmpInstanceName());
        resourceChargeRecord.setComponentCode(resourceMeterageRecord.getComponentCode());
        resourceChargeRecord.setMeterageId(chargeItemDTO.getMeterageItemId());
        resourceChargeRecord.setChargeId(chargeItemDTO.getUuid());
        // 适配折扣信息
        DiscountItemVO priorityDiscount = this.getDiscountItemVO(resourceMeterageRecord, disCountItemList);
        resourceChargeRecord.setDiscountId(priorityDiscount.getUuid());
        // 设置用户信息
        resourceChargeRecord.setOrderNo(resourceMeterageRecord.getOrderId());
        resourceChargeRecord.setTenantId(resourceMeterageRecord.getTenantId());
        resourceChargeRecord.setProjectId(resourceMeterageRecord.getProjectId());
        // 计费项创建时间
        resourceChargeRecord.setChargeBeginTime(chargeItemDTO.getCreateTime());
        // 计费开始时间
        resourceChargeRecord.setBeginUseTime(LocalDateTime.now());
        // 本次账单周期，以当前创建时间为准
        resourceChargeRecord.setBillCycleTime(LocalDateTime.now());
        // 计费单位
        Integer chargeFlavorUnit = chargeItemDTO.getChargeFlavorUnit();
        String unitName = ChargeFlavorUnitEnum.getUnitName(chargeFlavorUnit);
        Integer chargeFlavorTime = chargeItemDTO.getChargeFlavorTime();
        String timeName = ChargeFlavorTimeEnum.getTimeName(chargeFlavorTime);
        resourceChargeRecord.setChargeUnit(ChargeConstant.UNITARY + "/" + unitName + "/" + timeName);
        // 设置用量，云硬盘之外，其他都设置为1，按台/个进行计算
        JSONObject jsonData = JSONObject.parseObject(resourceMeterageRecord.getData());
        if (resourceMeterageRecord.getComponentCode().equals(ChargeConstant.RESOURCECENTER_COMPUTER_BLOCKSTORAGE)) {
            resourceChargeRecord.setChargeUsage(jsonData.getInteger(ChargeConstant.VOLUMN_SIZE));
        } else {
            resourceChargeRecord.setChargeUsage(1);
        }
        // 折前单价
        resourceChargeRecord.setPrice(chargeItemDTO.getPrice());
        // 折扣
        resourceChargeRecord.setDiscount(priorityDiscount.getCurrentDiscount());
        // 服务正常上线
        resourceChargeRecord.setResourceOffFlag(0);
        // 启用状态
        resourceChargeRecord.setEnableFlag(EnableStatusEnum.ENABLE.getStatus());
        // 设置资源数据JSON
        resourceChargeRecord.setResourceData(resourceMeterageRecord.getData());
        resourceChargeRecord.setDelFlag(DeleteFlagStatusEnum.VIEW.getStatus());
        resourceChargeRecord.setCreateTime(LocalDateTime.now());

        // 设置资源名称
        String nameKey = ResourceViewNameConvertEnum.getNameKey(resourceChargeRecord.getComponentCode());
        if (StringUtils.isBlank(nameKey)) {
            nameKey = "name";
        }
        String resourceData = resourceChargeRecord.getResourceData();
        if (StringUtils.isNotBlank(resourceData)) {
            JSONObject jsonObject = JSONObject.parseObject(resourceData);
            if (jsonObject.get(nameKey) != null) {
                String appName = jsonObject.getString(nameKey);
                resourceChargeRecord.setAppName(appName);
            }
        }
        return resourceChargeRecord;
    }

    @Override
    public Long accountTime(ChargeItem chargeItem, LocalDateTime startTime, LocalDateTime finishedTime) {
        // 计算时长
        Integer timeUnit = chargeItem.getChargeFlavorTime();
        long totalAccountTime = 0;
        switch (timeUnit) {
            case 1:
                // 时数差
                totalAccountTime = DateTimeConvertUtil.hourDiff(startTime, finishedTime);
                break;
            case 2:
                // 天数差
                totalAccountTime = DateTimeConvertUtil.dayDiff(startTime, finishedTime);
                break;
            case 3:
                // 月数差
                totalAccountTime = DateTimeConvertUtil.monthDiff(startTime, finishedTime);
                break;
            case 5:
                // 年数差
                totalAccountTime = DateTimeConvertUtil.yearDiff(startTime, finishedTime);
                break;
            default:
                throw new BusinessException(BusinessEnum.TIME_UNIT_ERROR);
        }
        return totalAccountTime;
    }

    @Override
    public Long getTime(ChargeItem chargeItem, LocalDateTime startTime, LocalDateTime finishedTime) {
        // 计算时长
        Integer timeUnit = chargeItem.getChargeFlavorTime();
        long totalAccountTime = 0;
        switch (timeUnit) {
            case 1:
                // 时数差
                totalAccountTime = ChronoUnit.HOURS.between(startTime, finishedTime);
                break;
            case 2:
                // 天数差
                totalAccountTime = ChronoUnit.DAYS.between(startTime, finishedTime);
                break;
            case 3:
                // 月数差
                totalAccountTime = ChronoUnit.MONTHS.between(startTime, finishedTime);
                break;
            case 5:
                // 年数差
                totalAccountTime = ChronoUnit.YEARS.between(startTime, finishedTime);
                break;
            default:
                throw new BusinessException(BusinessEnum.TIME_UNIT_ERROR);
        }
        return totalAccountTime;
    }

    /**
     * 适配折扣项
     * @param resourceMeterageRecord
     * @param disCountItemList
     * @return
     */
    private DiscountItemVO getDiscountItemVO(ResourceMeterageRecord resourceMeterageRecord, List<DiscountItemVO> disCountItemList) {
        DiscountItemVO priorityDiscount = null;
        /**
         * 匹配折扣，优先级 Project > Tenant > 系统级
         */
        for (DiscountItemVO discountItemVO : disCountItemList) {
            if (null == discountItemVO.getProjectId()) {
                continue;
            }
            priorityDiscount = discountItemVO;
        }
        if (null == priorityDiscount) {
            // 没有查询到Project折扣，匹配租户折扣
            for (DiscountItemVO discountItemVO : disCountItemList) {
                // 系统级别租户
                if (discountItemVO.getTenantId().equals(-1)) {
                    continue;
                }
                if (discountItemVO.getTenantId().equals(resourceMeterageRecord.getTenantId())) {
                    // tenant级别折扣
                    priorityDiscount = discountItemVO;
                }
            }
        }
        if (null == priorityDiscount) {
            // 没有租户及Project级别租户，只有系统级别租户，直接用系统级别租户
            for (DiscountItemVO discountItemVO : disCountItemList) {
                // 系统级别租户
                if (discountItemVO.getTenantId().equals(-1)) {
                    priorityDiscount = discountItemVO;
                }
            }
        }
        return priorityDiscount;
    }

    /**
     * 按照条件导出计费详情数据
     *
     * @param resourceChargeRecordDTO
     * @return
     */
    private List<ResourceChargeRecordVO> exportDataList(ResourceChargeRecordDTO resourceChargeRecordDTO) {
        // 权限控制
        R<AuthCheck> authCheckR = remoteAuthService.newAuthCheck("select_charge_record_list", null, null, null);
        log.info("authCheckR value is {}", JSONObject.toJSONString(authCheckR));
        if (authCheckR.getCode() != 0 || !authCheckR.getData().isStatus()) {
            throw new BusinessException("无访问权限");
        }
        log.info("resourceChargeRecordDTO = {}", JSONObject.toJSONString(resourceChargeRecordDTO));
        if (null == resourceChargeRecordDTO.getBillCycleTime()) {
            resourceChargeRecordDTO.setBillCycleTime(LocalDateTime.now());
        }
        // 构造查询条件
        LambdaQueryWrapper<ResourceChargeRecord> chargeBillDetailLambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(resourceChargeRecordDTO.getTenantId() != null, ResourceChargeRecord::getTenantId, resourceChargeRecordDTO.getTenantId())
                .eq(resourceChargeRecordDTO.getProjectId() != null, ResourceChargeRecord::getProjectId, resourceChargeRecordDTO.getProjectId())
                .apply(" YEAR(bill_cycle_time) = {0} AND MONTH(bill_cycle_time) = {1}", resourceChargeRecordDTO.getBillCycleTime().getYear(), resourceChargeRecordDTO.getBillCycleTime().getMonthValue())
                .eq(resourceChargeRecordDTO.getMeterageId() != null, ResourceChargeRecord::getMeterageId, resourceChargeRecordDTO.getMeterageId())
                .isNotNull(ResourceChargeRecord::getTotalCharge)
                .like(StringUtils.isNotBlank(resourceChargeRecordDTO.getCmpInstanceName()), ResourceChargeRecord::getAppName, resourceChargeRecordDTO.getCmpInstanceName());
        //多租户过滤
        List<Integer> tenantIds = new ArrayList<>();
        if (null != resourceChargeRecordDTO.getTenantId()) {
            R<List<Integer>> childrenByTenantId = remoteTenantService.getChildrenByTenantId(resourceChargeRecordDTO.getTenantId());
            if (CollectionUtils.isNotEmpty(childrenByTenantId.getData())) {
                tenantIds.addAll(childrenByTenantId.getData());
            }
            tenantIds.add(resourceChargeRecordDTO.getTenantId());
            if (CollectionUtils.isNotEmpty(authCheckR.getData().getTenantIds()) && !authCheckR.getData().getTenantIds().contains(resourceChargeRecordDTO.getTenantId())) {
                return new ArrayList<>();
            }
            chargeBillDetailLambdaQueryWrapper.in(ResourceChargeRecord::getTenantId, tenantIds);
        }
        // 判断配置的数据权限级别
        switch (authCheckR.getData().getOperatingRange()) {
            // 超级管理员，全局权限
            case OperatingRangeEnum.ALL_CODE:
                chargeBillDetailLambdaQueryWrapper.orderByDesc(ResourceChargeRecord::getCreateTime);
                break;
            //租户权限，返回值是tenantids集合
            case OperatingRangeEnum.TENANT_CODE:
                chargeBillDetailLambdaQueryWrapper.in(authCheckR.getData() != null, ResourceChargeRecord::getTenantId, authCheckR.getData().getTenantIds()).orderByDesc(ResourceChargeRecord::getCreateTime);
                break;
            // 返回值是projectIds集合
            case OperatingRangeEnum.PROJECT_CODE:
            //用户权限，返回值是userId
            case OperatingRangeEnum.SELF_CODE:
                throw new BusinessException("权限配置错误，账单详情查询只有系统级别和VDC级别查询");
            default:
                throw new BusinessException("权限配置错误，账单详情查询只有系统级别和VDC级别查询");
        }
        List<ResourceChargeRecord> chargeBillDetailList = resourceChargeRecordMapper.selectList(chargeBillDetailLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(chargeBillDetailList)) {
            return new ArrayList<>();
        }
        // Entity转换界面展示层的VO
        List<ResourceChargeRecordVO> resourceChargeRecordVOS = new ArrayList<>();
        List<String> chargeItemUuidList = new ArrayList<>();
        for (ResourceChargeRecord record : chargeBillDetailList) {
            ResourceChargeRecordVO resourceChargeRecordVO = new ResourceChargeRecordVO();
            BeanUtils.copyProperties(record, resourceChargeRecordVO);
            resourceChargeRecordVOS.add(resourceChargeRecordVO);
            if (!chargeItemUuidList.contains(record.getChargeId())) {
                chargeItemUuidList.add(record.getChargeId());
            }
        }
        List<ChargeItem> chargeItemList = chargeItemMapper.selectList(Wrappers.<ChargeItem>lambdaQuery().in(ChargeItem::getUuid, chargeItemUuidList));
        Map<String, ChargeItem> chargeItemMap = chargeItemList.stream().collect(Collectors.toMap(ChargeItem::getUuid, (p) -> p));
        for (ResourceChargeRecordVO record : resourceChargeRecordVOS) {
            ChargeItem chargeItem = chargeItemMap.get(record.getChargeId());
            if (chargeItem != null) {
                String unit;
                String usageTime;
                switch (chargeItem.getChargeFlavorUnit()) {
                    case 2:
                        unit = "个";
                        break;
                    case 3:
                        unit = "G";
                        break;
                    case 5:
                        unit = "T";
                        break;
                    case 6:
                        unit = "Core";
                        break;
                    case 7:
                        unit = "Partition区";
                        break;
                    default:
                        unit = "台";
                }
                switch (chargeItem.getChargeFlavorTime()) {
                    case 3:
                        usageTime = "月";
                        break;
                    case 5:
                        usageTime = "年";
                        break;
                    default:
                        usageTime = "天";
                }
                record.setDurationString(record.getDuration().toString() + usageTime);
                record.setChargeUsageString(record.getChargeUsage().toString() + unit);
                // 设置折扣
                if (record.getDiscount() != null) {
                    BigDecimal multiply = record.getDiscount().multiply(new BigDecimal("10"));
                    record.setDiscount(multiply);
                }
                // 单价、折扣、总费用保留2位小数
                DecimalFormat df = new DecimalFormat("#0.00");
                record.setPriceStr(df.format(record.getPrice()));
                record.setDiscountStr(df.format(record.getDiscount()));
                record.setTotalChargeStr(df.format(record.getTotalCharge()));
            }
        }
        // 获取计费项uuid集合
        List<String> chargeItemIds = new ArrayList<>(resourceChargeRecordVOS.stream().map(billDetailVO -> billDetailVO.getChargeId()).collect(Collectors.toSet()));
        log.info("chargeItemIds = {}", CollectionUtils.isEmpty(chargeItemIds) ? null : JSONObject.toJSONString(chargeItemIds));
        if (CollectionUtils.isEmpty(chargeItemIds)) {
            return new ArrayList<>();
        }
        // 计费项集合(已删除的也会被查询)
        IMeterageChargeItemService chargeItemService = SpringContextHolder.getBean(IMeterageChargeItemService.class);
        List<ChargeItemVO> chargeItems = chargeItemService.selectChargeItemListByChargeItemIds(chargeItemIds);
        log.info("chargeItems={}", CollectionUtils.isEmpty(chargeItems) ? null : JSONObject.toJSONString(chargeItems));
        if (CollectionUtils.isEmpty(chargeItems)) {
            return new ArrayList<>();
        }
        Map<String, ChargeItemVO> chargeItemVOMap = chargeItems.stream().collect(Collectors.toMap(ChargeItemVO::getUuid, (p) -> p));
        // 计量项集合
        List<Integer> meterIds = chargeItems.stream().map(chargeItemVO -> chargeItemVO.getMeterageItemId()).collect(Collectors.toList());
        LambdaQueryWrapper<MeterageProject> queryWrapper = Wrappers.<MeterageProject>lambdaQuery()
                .in(CollectionUtils.isNotEmpty(meterIds), MeterageProject::getId, meterIds)
                .eq(MeterageProject::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        List<MeterageProject> meterageItems = meterageProjectMapper.selectList(queryWrapper);
        log.info("meterageItems={}", CollectionUtils.isEmpty(meterageItems) ? null : JSONObject.toJSONString(meterageItems));
        if (CollectionUtils.isEmpty(meterageItems)) {
            return new ArrayList<>();
        }
        Map<Integer, MeterageProject> meterageProjectMap = meterageItems.stream().collect(Collectors.toMap(MeterageProject::getId, (p) -> p));

        // 填充计费项/计量项名称等数据
        for (ResourceChargeRecordVO resourceChargeRecordVO : resourceChargeRecordVOS) {
            // 设置计费项名称
            ChargeItemVO dbChargeItemVO = chargeItemVOMap.get(resourceChargeRecordVO.getChargeId());
            if (dbChargeItemVO != null) {
                resourceChargeRecordVO.setChargeName(dbChargeItemVO.getChargeName());
            }

            MeterageProject meterageProject = meterageProjectMap.get(resourceChargeRecordVO.getMeterageId());
            if (meterageProject != null) {
                // 设置计量项名称
                resourceChargeRecordVO.setMeterageName(meterageProject.getName());
            }
        }
        log.info("chargeBillDetailVOIPage = {}", JSONObject.toJSONString(resourceChargeRecordVOS));
        return resourceChargeRecordVOS;
    }

    @Override
    public IPage<ResourceChargeRecordVO> selectPage(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        // 权限控制
        R<AuthCheck> authCheckR = remoteAuthService.newAuthCheck("select_charge_record_list", null, null, null);
        log.info("authCheckR value is {}", JSONObject.toJSONString(authCheckR));
        if (authCheckR.getCode() != 0 || !authCheckR.getData().isStatus()) {
            throw new BusinessException("无访问权限");
        }
        log.info("resourceChargeRecordDTO = {}", JSONObject.toJSONString(resourceChargeRecordDTO));
        if (resourceChargeRecordDTO.getCurrent() != null) {
            page.setCurrent(resourceChargeRecordDTO.getCurrent());
        }
        if (resourceChargeRecordDTO.getSize() != null) {
            page.setSize(resourceChargeRecordDTO.getSize());
        }
        if (null == resourceChargeRecordDTO.getBillCycleTime()) {
            resourceChargeRecordDTO.setBillCycleTime(LocalDateTime.now());
        }
        // 构造查询条件
        LambdaQueryWrapper<ResourceChargeRecord> chargeBillDetailLambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(resourceChargeRecordDTO.getTenantId() != null, ResourceChargeRecord::getTenantId, resourceChargeRecordDTO.getTenantId())
                .eq(resourceChargeRecordDTO.getProjectId() != null, ResourceChargeRecord::getProjectId, resourceChargeRecordDTO.getProjectId())
                .apply(" YEAR(bill_cycle_time) = {0} AND MONTH(bill_cycle_time) = {1}", resourceChargeRecordDTO.getBillCycleTime().getYear(), resourceChargeRecordDTO.getBillCycleTime().getMonthValue())
                .eq(resourceChargeRecordDTO.getMeterageId() != null, ResourceChargeRecord::getMeterageId, resourceChargeRecordDTO.getMeterageId())
                .isNotNull(ResourceChargeRecord::getTotalCharge)
                .like(StringUtils.isNotBlank(resourceChargeRecordDTO.getCmpInstanceName()), ResourceChargeRecord::getAppName, resourceChargeRecordDTO.getCmpInstanceName());
        //多租户过滤
        List<Integer> tenantIds = new ArrayList<>();
        if (null != resourceChargeRecordDTO.getTenantId()) {
            R<List<Integer>> childrenByTenantId = remoteTenantService.getChildrenByTenantId(resourceChargeRecordDTO.getTenantId());
            if (CollectionUtils.isNotEmpty(childrenByTenantId.getData())) {
                tenantIds.addAll(childrenByTenantId.getData());
            }
            tenantIds.add(resourceChargeRecordDTO.getTenantId());
            if (CollectionUtils.isNotEmpty(authCheckR.getData().getTenantIds()) && !authCheckR.getData().getTenantIds().contains(resourceChargeRecordDTO.getTenantId())) {
                return new Page<>(1, 10);
            }
            chargeBillDetailLambdaQueryWrapper.in(ResourceChargeRecord::getTenantId, tenantIds);
        }
        // 判断配置的数据权限级别
        switch (authCheckR.getData().getOperatingRange()) {
            // 超级管理员，全局权限
            case OperatingRangeEnum.ALL_CODE:
                chargeBillDetailLambdaQueryWrapper.orderByDesc(ResourceChargeRecord::getCreateTime);
                break;
            //租户权限，返回值是tenantids集合
            case OperatingRangeEnum.TENANT_CODE:
                chargeBillDetailLambdaQueryWrapper.in(authCheckR.getData() != null, ResourceChargeRecord::getTenantId, authCheckR.getData().getTenantIds()).orderByDesc(ResourceChargeRecord::getCreateTime);
                break;
            // 返回值是projectIds集合
            case OperatingRangeEnum.PROJECT_CODE:
            //用户权限，返回值是userId
            case OperatingRangeEnum.SELF_CODE:
                throw new BusinessException("权限配置错误，账单详情查询只有系统级别和VDC级别查询");
            default:
                throw new BusinessException("权限配置错误，账单详情查询只有系统级别和VDC级别查询");
        }
        IPage<ResourceChargeRecord> chargeBillDetailPage = resourceChargeRecordMapper.selectPage(page, chargeBillDetailLambdaQueryWrapper);
        IPage<ResourceChargeRecordVO> chargeBillDetailVOIPage = new Page<>();
        List<ResourceChargeRecord> resourceChargeRecords = chargeBillDetailPage.getRecords();
        if (CollectionUtils.isEmpty(resourceChargeRecords)) {
            return chargeBillDetailVOIPage;
        }
        // Entity转换界面展示层的VO
        chargeBillDetailVOIPage.setPages(chargeBillDetailPage.getPages());
        chargeBillDetailVOIPage.setTotal(chargeBillDetailPage.getTotal());
        chargeBillDetailVOIPage.setSize(chargeBillDetailPage.getSize());
        chargeBillDetailVOIPage.setCurrent(chargeBillDetailPage.getCurrent());
        List<ResourceChargeRecordVO> resourceChargeRecordVOS = new ArrayList<>();
        List<String> chargeItemUuidList = new ArrayList<>();
        for (ResourceChargeRecord record : chargeBillDetailPage.getRecords()) {
            ResourceChargeRecordVO resourceChargeRecordVO = new ResourceChargeRecordVO();
            BeanUtils.copyProperties(record, resourceChargeRecordVO);
            // 格式化账单周期
            LocalDateTime billCycleTime = resourceChargeRecordVO.getBillCycleTime();
            if (billCycleTime != null) {
                resourceChargeRecordVO.setBillCycleTimeStr(DateTimeConvertUtil.getBillCycleTimeStr(billCycleTime));
            }
            resourceChargeRecordVOS.add(resourceChargeRecordVO);
            if (!chargeItemUuidList.contains(record.getChargeId())) {
                chargeItemUuidList.add(record.getChargeId());
            }
        }
        List<ChargeItem> chargeItemList = chargeItemMapper.selectList(Wrappers.<ChargeItem>lambdaQuery().in(ChargeItem::getUuid, chargeItemUuidList));
        Map<String, ChargeItem> chargeItemMap = chargeItemList.stream().collect(Collectors.toMap(ChargeItem::getUuid, (p) -> p));
        for (ResourceChargeRecordVO record : resourceChargeRecordVOS) {
            ChargeItem chargeItem = chargeItemMap.get(record.getChargeId());
            if (chargeItem != null) {
                String unit;
                String usageTime;
                switch (chargeItem.getChargeFlavorUnit()) {
                    case 2:
                        unit = "个";
                        break;
                    case 3:
                        unit = "G";
                        break;
                    case 5:
                        unit = "T";
                        break;
                    case 6:
                        unit = "Core";
                        break;
                    case 7:
                        unit = "Partition区";
                        break;
                    default:
                        unit = "台";
                }
                switch (chargeItem.getChargeFlavorTime()) {
                    case 3:
                        usageTime = "月";
                        break;
                    case 5:
                        usageTime = "年";
                        break;
                    default:
                        usageTime = "天";
                }
                record.setDurationString(record.getDuration().toString() + usageTime);
                record.setChargeUsageString(record.getChargeUsage().toString() + unit);
                // 设置折扣
                if (record.getDiscount() != null) {
                    BigDecimal multiply = record.getDiscount().multiply(new BigDecimal("10"));
                    record.setDiscount(multiply);
                }
                // 单价、折扣、总费用保留2位小数
                DecimalFormat df = new DecimalFormat("#0.00");
                record.setPriceStr(df.format(record.getPrice()));
                record.setDiscountStr(df.format(record.getDiscount()));
                record.setTotalChargeStr(df.format(record.getTotalCharge()));
            }
        }
        chargeBillDetailVOIPage.setRecords(resourceChargeRecordVOS);

        // 获取计费项uuid集合
        List<ResourceChargeRecordVO> billDetailVOS = chargeBillDetailVOIPage.getRecords();
        List<String> chargeItemIds = new ArrayList<>(billDetailVOS.stream().map(billDetailVO -> billDetailVO.getChargeId()).collect(Collectors.toSet()));
        log.info("chargeItemIds = {}", CollectionUtils.isEmpty(chargeItemIds) ? null : JSONObject.toJSONString(chargeItemIds));
        if (CollectionUtils.isEmpty(chargeItemIds)) {
            return chargeBillDetailVOIPage;
        }

        // 计费项集合(已删除的也会被查询)
        IMeterageChargeItemService chargeItemService = SpringContextHolder.getBean(IMeterageChargeItemService.class);
        List<ChargeItemVO> chargeItems = chargeItemService.selectChargeItemListByChargeItemIds(chargeItemIds);
        log.info("chargeItems={}", CollectionUtils.isEmpty(chargeItems) ? null : JSONObject.toJSONString(chargeItems));
        if (CollectionUtils.isEmpty(chargeItems)) {
            return chargeBillDetailVOIPage;
        }
        Map<String, ChargeItemVO> chargeItemVOMap = chargeItems.stream().collect(Collectors.toMap(ChargeItemVO::getUuid, (p) -> p));

        // 计量项集合
        List<Integer> meterIds = chargeItems.stream().map(chargeItemVO -> chargeItemVO.getMeterageItemId()).collect(Collectors.toList());
        LambdaQueryWrapper<MeterageProject> queryWrapper = Wrappers.<MeterageProject>lambdaQuery()
                .in(CollectionUtils.isNotEmpty(meterIds), MeterageProject::getId, meterIds)
                .eq(MeterageProject::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        List<MeterageProject> meterageItems = meterageProjectMapper.selectList(queryWrapper);
        log.info("meterageItems={}", CollectionUtils.isEmpty(meterageItems) ? null : JSONObject.toJSONString(meterageItems));
        if (CollectionUtils.isEmpty(meterageItems)) {
            return chargeBillDetailVOIPage;
        }
        Map<Integer, MeterageProject> meterageProjectMap = meterageItems.stream().collect(Collectors.toMap(MeterageProject::getId, (p) -> p));

        // 填充计费项/计量项名称等数据
        for (ResourceChargeRecordVO resourceChargeRecordVO : chargeBillDetailVOIPage.getRecords()) {
            // 设置计费项名称
            ChargeItemVO dbChargeItemVO = chargeItemVOMap.get(resourceChargeRecordVO.getChargeId());
            if (dbChargeItemVO != null) {
                resourceChargeRecordVO.setChargeName(dbChargeItemVO.getChargeName());
            }

            MeterageProject meterageProject = meterageProjectMap.get(resourceChargeRecordVO.getMeterageId());
            if (meterageProject != null) {
                // 设置计量项名称
                resourceChargeRecordVO.setMeterageName(meterageProject.getName());
            }
        }
        log.info("chargeBillDetailVOIPage = {}", JSONObject.toJSONString(chargeBillDetailVOIPage.getRecords()));
        return chargeBillDetailVOIPage;
    }

    @Override
    public List<ResourceChargeRecord> selectChargeBillDetailListByCmpInstanceNameList(List<String> cmpInstanceList) {
        LambdaQueryWrapper<ResourceChargeRecord> lambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .in(ResourceChargeRecord::getCmpInstanceName, cmpInstanceList)
                .isNull(ResourceChargeRecord::getFinishUseTime)
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        return resourceChargeRecordMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<ResourceChargeRecord> selectChargeDisableBilllList(List<String> cmpInstanceList) {
        LambdaQueryWrapper<ResourceChargeRecord> lambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .in(ResourceChargeRecord::getCmpInstanceName, cmpInstanceList)
                .isNotNull(ResourceChargeRecord::getFinishUseTime)
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.DISABLE.getStatus())
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        return resourceChargeRecordMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<ResourceChargeRecord> selectChargeBillDetailListByChargeItemId(String chargeItemId) {
        // 查询匹配某个计费项的所有资源
        LambdaQueryWrapper<ResourceChargeRecord> lambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getChargeId, chargeItemId)
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ResourceChargeRecord> resourceChargeRecords = resourceChargeRecordMapper.selectList(lambdaQueryWrapper);
        // 过滤计费账单记录中尚未结算的资源账单记录
        List<ResourceChargeRecord> chargeRecords = resourceChargeRecords.stream()
                .filter(resourceChargeRecord -> resourceChargeRecord.getBeginUseTime() != null && null == resourceChargeRecord.getFinishUseTime())
                .collect(Collectors.toList());
        return chargeRecords;
    }

    @Override
    public List<ResourceChargeRecord> selectAccountChargeBillDetailListByChargeItemId(String chargeItemId) {
        // 查询匹配某个计费项的所有资源
        LambdaQueryWrapper<ResourceChargeRecord> lambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getChargeId, chargeItemId)
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ResourceChargeRecord> resourceChargeRecords = resourceChargeRecordMapper.selectList(lambdaQueryWrapper);
        // 过滤计费账单记录中尚未结算的资源账单记录
        List<ResourceChargeRecord> chargeRecords = resourceChargeRecords.stream()
                .filter(resourceChargeRecord -> resourceChargeRecord.getBeginUseTime() != null && resourceChargeRecord.getFinishUseTime() != null && resourceChargeRecord.getEnableFlag().equals(1))
                .collect(Collectors.toList());
        return chargeRecords;
    }

    @Override
    public void insertChargeBill(List<ResourceChargeRecord> billDetails, ChargeItemDTO chargeItemDTO) {
        if (CollectionUtils.isEmpty(billDetails)) {
            return;
        }
        // 启用之后重新新增资源计费记录数据
        List<ResourceChargeRecord> insertResourceChargeRecords = new ArrayList<>();
        for (ResourceChargeRecord resourceChargeRecord : billDetails) {
            resourceChargeRecord.setUuid(UUID.randomUUID().toString());
            if (chargeItemDTO != null) {
                // 折前单价变更
                if (chargeItemDTO.getPrice() != null) {
                    resourceChargeRecord.setPrice(chargeItemDTO.getPrice());
                }
                // ------------折扣价格发生变更(暂时未适配)---------------------//

                // 计费单位变更
                String unitName = null;
                if (chargeItemDTO.getChargeFlavorUnit() != null) {
                    Integer chargeFlavorUnit = chargeItemDTO.getChargeFlavorUnit();
                    unitName = ChargeFlavorUnitEnum.getUnitName(chargeFlavorUnit);
                }
                String timeName = null;
                if (chargeItemDTO.getChargeFlavorTime() != null) {
                    Integer chargeFlavorTime = chargeItemDTO.getChargeFlavorTime();
                    timeName = ChargeFlavorTimeEnum.getTimeName(chargeFlavorTime);
                }
                if (StringUtils.isNotBlank(unitName) && StringUtils.isNotBlank(timeName)) {
                    resourceChargeRecord.setChargeUnit(ChargeConstant.UNITARY + "/" + unitName + "/" + timeName);
                }
                // 设置备注
                resourceChargeRecord.setRemark(chargeItemDTO.getRemark());
            }

            // 设置资源名称
            String nameKey = ResourceViewNameConvertEnum.getNameKey(resourceChargeRecord.getComponentCode());
            if (StringUtils.isBlank(nameKey)) {
                nameKey = "name";
            }
            String resourceData = resourceChargeRecord.getResourceData();
            if (StringUtils.isNotBlank(resourceData)) {
                JSONObject jsonObject = JSONObject.parseObject(resourceData);
                if (jsonObject.get(nameKey) != null) {
                    String appName = jsonObject.getString(nameKey);
                    resourceChargeRecord.setAppName(appName);
                }
            }

            // 计费开始时间
            resourceChargeRecord.setBeginUseTime(LocalDateTime.now());
            resourceChargeRecord.setBillCycleTime(LocalDateTime.now());
            resourceChargeRecord.setCreateTime(LocalDateTime.now());
            resourceChargeRecord.setFinishUseTime(null);
            resourceChargeRecord.setTotalCharge(null);
            resourceChargeRecord.setUpdateTime(null);
            resourceChargeRecord.setDelFlag(DeleteFlagStatusEnum.VIEW.getStatus());
            insertResourceChargeRecords.add(resourceChargeRecord);
        }
        resourceChargeRecordMapper.saveChargeBillDetailList(insertResourceChargeRecords);
    }

    @Override
    public boolean totalChargeBillRecord() {
        log.info("====== Total charge bill start ======");
        // 查询当前所有的计费记录 未删除 启用 正常服务未下线 未结算
        LambdaQueryWrapper<ResourceChargeRecord> queryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .isNull(ResourceChargeRecord::getFinishUseTime);
        List<ResourceChargeRecord> resourceChargeRecords = resourceChargeRecordMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(resourceChargeRecords)) {
            log.info("========== Total charge bill end，未查到需要结算得资源数据！==========");
            return true;
        }
        // 需要查询的次数,也就是总共多少页
        List<List<ResourceChargeRecord>> partition = Lists.partition(resourceChargeRecords, SIZE);
        //添加任务，分批次处理
        for (List<ResourceChargeRecord> records : partition) {
            MonthTotalChargeBillThread monthTotalChargeBillThread = new MonthTotalChargeBillThread(records);
            threadPoolTaskExecutor.execute(monthTotalChargeBillThread);
        }
        log.info("====== Total charge bill end ======");
        return true;
    }

    @Override
    public void exportChargeBillDetail(HttpServletResponse response, Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        log.info("========== Charge bill item export ==========");
        // 获取需要导出的数据
        List<ResourceChargeRecordVO> resourceChargeRecordVOS = this.exportDataList(resourceChargeRecordDTO);
        // 格式化时间
        if (CollectionUtils.isNotEmpty(resourceChargeRecordVOS)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter dateTimeFormatterForCycle = DateTimeFormatter.ofPattern("yyyy-MM");
            for (ResourceChargeRecordVO resourceChargeRecordVO : resourceChargeRecordVOS) {
                if (resourceChargeRecordVO.getChargeBeginTime() != null) {
                    resourceChargeRecordVO.setChargeBeginTimeStr(dateTimeFormatter.format(resourceChargeRecordVO.getChargeBeginTime()));
                }
                if (resourceChargeRecordVO.getBeginUseTime() != null) {
                    resourceChargeRecordVO.setBeginUseTimeStr(dateTimeFormatter.format(resourceChargeRecordVO.getBeginUseTime()));
                }
                if (resourceChargeRecordVO.getFinishUseTime() != null) {
                    resourceChargeRecordVO.setFinishUseTimeStr(dateTimeFormatter.format(resourceChargeRecordVO.getFinishUseTime()));
                }
                if (resourceChargeRecordVO.getBillCycleTime() != null) {
                    resourceChargeRecordVO.setBillCycleTimeStr(dateTimeFormatterForCycle.format(resourceChargeRecordVO.getBillCycleTime()));
                }
            }
        }
        // 获取导出模板
        String xmlTemplateName = resourceChargeRecordDTO.getXmlTemplateName();
        String fileName = "classpath:export/" + xmlTemplateName + ".xml";
        // 调用导出
        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            InputStream inputStream = resourceLoader.getResource(fileName).getInputStream();
            FieldsSettings fieldsSettings = JaxbUtil.converyToJavaBean(inputStream, FieldsSettings.class);
            if (fieldsSettings != null) {
                List<FieldSetting> fieldSetting = fieldsSettings.getFieldSetting();
                String[] headers = new String[fieldsSettings.getFieldSetting().size()];
                List<FieldSetting> fields = new ArrayList<>(fieldsSettings.getFieldSetting().size());
                for (int i = 0; i < fieldSetting.size(); i++) {
                    headers[i] = fieldSetting.get(i).getFieldCnName();
                    fields.add(fieldSetting.get(i));
                }
                ExcelUtil<ResourceChargeRecordVO> ex = new ExcelUtil<>();
                String templateName = ExportTypeEnum.getTemplateName(xmlTemplateName);
                ex.exportExcel(templateName, headers, fields, resourceChargeRecordVOS, response.getOutputStream(), ChargeConstant.DATE_TYPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("========== Charge bill item end ==========");
    }

    @Override
    public IPage<ResourceChargeRecordVO> totalTenantMonth(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        LocalDateTime tenantTotalMonth = resourceChargeRecordDTO.getTenantTotalMonth();
        if (null == tenantTotalMonth) {
            tenantTotalMonth = LocalDateTime.now();
        }
        IPage<ResourceChargeRecordVO> resourceChargeRecordVOIPage = resourceChargeRecordMapper.listTenantMonthCondition(page, tenantTotalMonth.getYear(), tenantTotalMonth.getMonthValue());
        // 处理租户名称
        return this.setTenantName(resourceChargeRecordVOIPage);
    }

    private IPage<ResourceChargeRecordVO> setTenantName(IPage<ResourceChargeRecordVO> resourceChargeRecordVOIPage) {
        List<ResourceChargeRecordVO> records = resourceChargeRecordVOIPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return resourceChargeRecordVOIPage;
        }
        Set<Integer> tenantIdSet = new HashSet<>();
        for (ResourceChargeRecordVO record : records) {
            Integer tenantId = record.getTenantId();
            if (null == tenantId) {
                continue;
            }
            tenantIdSet.add(tenantId);
        }
        if (CollectionUtils.isEmpty(tenantIdSet)) {
            throw new BusinessException(BusinessEnum.USER_TENANT_ID_NULL);
        }
        TenantDTO tenantDTO = new TenantDTO();
        List<Integer> tenantIds = new ArrayList<>(tenantIdSet);
        tenantDTO.setTenantIds(tenantIds);
        // 查询租户列表
        R<List<Tenant>> listR = remoteTenantService.getListInfo(tenantDTO);
        List<Tenant> tenantList = listR.getData();
        log.info("tenantList = {}", JSONObject.toJSONString(tenantList));
        if (CollectionUtils.isEmpty(tenantList)) {
            return resourceChargeRecordVOIPage;
        }
        Map<Integer, Tenant> collect = tenantList.stream().collect(Collectors.toMap(Tenant::getId, (p) -> p));
        log.info("collect = {}", JSONObject.toJSONString(collect));
        for (ResourceChargeRecordVO record : records) {
            if (collect.get(record.getTenantId()) != null) {
                // 填充租户名称
                record.setTenantName(collect.get(record.getTenantId()).getName());
            }
        }
        return resourceChargeRecordVOIPage;
    }

    @Override
    public IPage<ResourceChargeRecordVO> totalTenantYear(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        Integer startCount = 0;
        Integer endCount = resourceChargeRecordDTO.getTenantYearStatus().equals(0) ? 6 : 12;
        log.info("startCount = {} ,endCount = {}", startCount, endCount);
        IPage<ResourceChargeRecordVO> resourceChargeRecordVOIPage = resourceChargeRecordMapper.listTenantYearCondition(page, startCount, endCount);
        // 处理租户名称
        return this.setTenantName(resourceChargeRecordVOIPage);
    }

    @Override
    public IPage<ResourceChargeRecordVO> totalProjectMonth(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        LocalDateTime projectTotalMonth = resourceChargeRecordDTO.getProjectTotalMonth();
        if (null == projectTotalMonth) {
            projectTotalMonth = LocalDateTime.now();
        }
        IPage<ResourceChargeRecordVO> resourceChargeRecordVOIPage = resourceChargeRecordMapper.listProjectMonthCondition(page, projectTotalMonth.getYear(), projectTotalMonth.getMonthValue());
        // 设置Project名称
        return this.setProjectName(resourceChargeRecordVOIPage);
    }

    private IPage<ResourceChargeRecordVO> setProjectName(IPage<ResourceChargeRecordVO> resourceChargeRecordVOIPage) {
        List<ResourceChargeRecordVO> records = resourceChargeRecordVOIPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return resourceChargeRecordVOIPage;
        }
        for (ResourceChargeRecordVO record : records) {
            R<ProjectVO> projectVOR = remoteProjectService.selectProject(record.getProjectId());
            ProjectVO projectVO = projectVOR.getData();
            if (null == projectVO) {
                continue;
            }
            record.setProjectName(projectVO.getName());
        }
        return resourceChargeRecordVOIPage;
    }

    @Override
    public IPage<ResourceChargeRecordVO> totalProjectYear(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        Integer startCount = 0;
        Integer endCount = resourceChargeRecordDTO.getProjectYearStatus().equals(0) ? 6 : 12;
        IPage<ResourceChargeRecordVO> resourceChargeRecordVOIPage = resourceChargeRecordMapper.listProjectYearCondition(page, startCount, endCount);
        // 设置Project名称
        return this.setProjectName(resourceChargeRecordVOIPage);
    }

    @Override
    public Map<String, Map<String, String>> totalTenantPrice() {
        Map<String, Map<String, String>> resultMap = new ConcurrentHashMap<>();
        List<ResourceChargeRecordVO> resourceChargeRecordVOS = resourceChargeRecordMapper.totalTenantPrice();
        if (CollectionUtils.isEmpty(resourceChargeRecordVOS)) {
            log.info("resourceChargeRecordVOS is empty !");
            return resultMap;
        }
        // 转换资源费用使用占比
        return this.getResourcePercent(resultMap, resourceChargeRecordVOS);
    }

    private Map<String, Map<String, String>> getResourcePercent(Map<String, Map<String, String>> resultMap, List<ResourceChargeRecordVO> resourceChargeRecordVOS) {
        // 处理资源名称
        Set<Integer> meterProjectIdsSet = resourceChargeRecordVOS.stream().map(resourceChargeRecordVO -> resourceChargeRecordVO.getMeterageId()).collect(Collectors.toSet());
        List<Integer> meterProjectIdList = new ArrayList<>(meterProjectIdsSet);
        log.info("meterProjectIdList = {}", meterProjectIdList);
        if (CollectionUtils.isEmpty(meterProjectIdList)) {
            return resultMap;
        }
        LambdaQueryWrapper<MeterageProject> queryWrapper = Wrappers.<MeterageProject>lambdaQuery().in(MeterageProject::getId, meterProjectIdList);
        List<MeterageProject> meterageProjects = meterageProjectMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(meterageProjects)) {
            log.info("meterageProjects is empty !");
            return resultMap;
        }
        // Map->key:meterageId,value:MeterageProject
        Map<Integer, MeterageProject> meterageProjectMap = meterageProjects.stream().collect(Collectors.toMap(MeterageProject::getId, (map) -> map));
        for (ResourceChargeRecordVO resourceChargeRecordVO : resourceChargeRecordVOS) {
            if (meterageProjectMap.get(resourceChargeRecordVO.getMeterageId()) != null) {
                resourceChargeRecordVO.setMeterageName(meterageProjectMap.get(resourceChargeRecordVO.getMeterageId()).getName());
            }
        }
        log.info("resourceChargeRecordVOS = {}", JSONObject.toJSONString(resourceChargeRecordVOS));
        // 初始化总费用
        BigDecimal totoalBigDecimal = new BigDecimal(INITIALIZATION);
        for (ResourceChargeRecordVO resourceChargeRecordVO : resourceChargeRecordVOS) {
            if (StringUtils.isBlank(resourceChargeRecordVO.getMeterageName())) {
                log.info("meterageProjects is null！");
                continue;
            }
            if (null == resourceChargeRecordVO.getTotalCount()) {
                continue;
            }
            totoalBigDecimal = totoalBigDecimal.add(resourceChargeRecordVO.getTotalCount());
        }
        log.info("totoalBigDecimal = {}", totoalBigDecimal);
        for (ResourceChargeRecordVO resourceChargeRecordVO : resourceChargeRecordVOS) {
            if (null == resourceChargeRecordVO.getMeterageId()) {
                log.info("meterageId is null！");
                continue;
            }
            if (StringUtils.isBlank(resourceChargeRecordVO.getMeterageName())) {
                log.info("meterageName is null！");
                continue;
            }
            MeterageProject meterageProject = meterageProjectMap.get(resourceChargeRecordVO.getMeterageId());
            if (null == meterageProject) {
                log.info("meterageProject is null!");
                continue;
            }
            // 获取某个资源的总费用
            BigDecimal totalCount = resourceChargeRecordVO.getTotalCount();
            String viewZhName = ResourceViewNameConvertEnum.getPercentNameKey(meterageProject.getComponentCode());
            if (StringUtils.isBlank(viewZhName)) {
                // viewZhName为空
                Map<String, String> stringStringMap = resultMap.get(ResourceViewNameConvertEnum.OTHERS_RESOURCE_ITEM.getComponentCode());
                if (CollectionUtils.isEmpty(stringStringMap)) {
                    // 其他费用首次key入库
                    Map<String, String> mapToPercent = new ConcurrentHashMap<>();
                    mapToPercent.put(ChargeConstant.VIEW_ZH_NAME, ResourceViewNameConvertEnum.OTHERS_RESOURCE_ITEM.getPercentName());
                    String percentFormat = "";
                    if (null == totalCount) {
                        percentFormat = ChargeConstant.DEFAULT_PERCENT;
                        mapToPercent.put(ChargeConstant.CHARGE, "0");
                    } else {
                        percentFormat = CountPercent.numFormat(totalCount, totoalBigDecimal);
                        mapToPercent.put(ChargeConstant.CHARGE, totalCount.toString());
                    }
                    mapToPercent.put(ChargeConstant.PERCENT, percentFormat);
                    resultMap.put(ResourceViewNameConvertEnum.OTHERS_RESOURCE_ITEM.getComponentCode(), mapToPercent);
                } else {
                    // 其他费用叠加
                    BigDecimal charge = new BigDecimal(stringStringMap.get(ChargeConstant.CHARGE));
                    charge = charge.add(resourceChargeRecordVO.getTotalCount());
                    String percentFormat = "";
                    if (null == totalCount) {
                        percentFormat = ChargeConstant.DEFAULT_PERCENT;
                        stringStringMap.put(ChargeConstant.CHARGE, "0");
                    } else {
                        percentFormat = CountPercent.numFormat(charge, totoalBigDecimal);
                        stringStringMap.put(ChargeConstant.CHARGE, charge.toString());
                    }
                    stringStringMap.put(ChargeConstant.PERCENT, percentFormat);
                    resultMap.put(ResourceViewNameConvertEnum.OTHERS_RESOURCE_ITEM.getComponentCode(), stringStringMap);
                }
            } else {
                // viewZhName不为空
                Map<String, String> mapToPercent = new ConcurrentHashMap<>();
                mapToPercent.put(ChargeConstant.VIEW_ZH_NAME, viewZhName);
                String percentFormat = "";
                if (null == totalCount) {
                    percentFormat = ChargeConstant.DEFAULT_PERCENT;
                    mapToPercent.put(ChargeConstant.CHARGE, "0");
                } else {
                    percentFormat = CountPercent.numFormat(totalCount, totoalBigDecimal);
                    mapToPercent.put(ChargeConstant.CHARGE, totalCount.toString());
                }
                mapToPercent.put(ChargeConstant.PERCENT, percentFormat);
                resultMap.put(meterageProject.getComponentCode(), mapToPercent);
            }
        }
        return resultMap;
    }

    @Override
    public List<ResourceChargeRecordVO> reviewTenantPrice(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        if (null == resourceChargeRecordDTO.getTenantId()) {
            throw new BusinessException(BusinessEnum.USER_TENANT_ID_NULL);
        }
        Integer startCount = 0;
        Integer endCount = resourceChargeRecordDTO.getTenantYearStatus().equals(0) ? 6 : 12;
        List<ResourceChargeRecordVO> resourceChargeRecordVOS = resourceChargeRecordMapper.listReviewTenantMonthCondition(startCount, endCount, resourceChargeRecordDTO.getTenantId());
        // 处理租户名称
        if (CollectionUtils.isEmpty(resourceChargeRecordVOS)) {
            return resourceChargeRecordVOS;
        }
        return this.setTenantListName(resourceChargeRecordVOS);
    }

    private List<ResourceChargeRecordVO> setTenantListName(List<ResourceChargeRecordVO> resourceChargeRecordVOS) {
        Set<Integer> tenantIdSet = resourceChargeRecordVOS.stream().map(resourceChargeRecordVO -> resourceChargeRecordVO.getTenantId()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(tenantIdSet)) {
            throw new BusinessException(BusinessEnum.USER_TENANT_ID_NULL);
        }
        TenantDTO tenantDTO = new TenantDTO();
        List<Integer> tenantIds = new ArrayList<>(tenantIdSet);
        tenantDTO.setTenantIds(tenantIds);
        R<List<Tenant>> listR = remoteTenantService.getListInfo(tenantDTO);
        List<Tenant> tenantList = listR.getData();
        if (CollectionUtils.isEmpty(tenantList)) {
            return resourceChargeRecordVOS;
        }
        Map<Integer, Tenant> collect = tenantList.stream().collect(Collectors.toMap(Tenant::getId, (p) -> p));
        for (ResourceChargeRecordVO record : resourceChargeRecordVOS) {
            if (collect.get(record.getTenantId()) != null) {
                // 填充租户名称
                record.setTenantName(collect.get(record.getTenantId()).getName());
            }
        }
        return resourceChargeRecordVOS;
    }

    @Override
    public List<ResourceChargeRecordVO> reviewProjectPrice(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO) {
        if (null == resourceChargeRecordDTO.getProjectId()) {
            throw new BusinessException(BusinessEnum.PROJECT_VALUE_IS_NULL);
        }
        Integer startCount = 0;
        Integer endCount = resourceChargeRecordDTO.getProjectYearStatus().equals(0) ? 6 : 12;
        List<ResourceChargeRecordVO> resourceChargeRecordVOS = resourceChargeRecordMapper.listReviewProjectMonthCondition(startCount, endCount, resourceChargeRecordDTO.getProjectId());
        return this.setProjectListName(resourceChargeRecordVOS);
    }

    private List<ResourceChargeRecordVO> setProjectListName(List<ResourceChargeRecordVO> resourceChargeRecordVOS) {
        if (CollectionUtils.isEmpty(resourceChargeRecordVOS)) {
            return resourceChargeRecordVOS;
        }
        for (ResourceChargeRecordVO record : resourceChargeRecordVOS) {
            R<ProjectVO> projectVOR = remoteProjectService.selectProject(record.getProjectId());
            ProjectVO projectVO = projectVOR.getData();
            if (null == projectVO) {
                continue;
            }
            record.setProjectName(projectVO.getName());
        }
        return resourceChargeRecordVOS;
    }

    @Override
    public Map<String, Map<String, String>> reviewTotalPrice(ResourceChargeRecordDTO resourceChargeRecordDTO) {
        Map<String, Map<String, String>> resultMap = new ConcurrentHashMap<>();
        List<ResourceChargeRecordVO> resourceChargeRecordVOS;
        if (resourceChargeRecordDTO.getTenantId() != null) {
            // 查询租户
            resourceChargeRecordVOS = resourceChargeRecordMapper.tenantMonthPrice(resourceChargeRecordDTO.getTenantTotalMonth().getYear(), resourceChargeRecordDTO.getTenantTotalMonth().getMonthValue(), resourceChargeRecordDTO.getTenantId());
        } else {
            // 查询project
            resourceChargeRecordVOS = resourceChargeRecordMapper.projectMonthPrice(resourceChargeRecordDTO.getProjectTotalMonth().getYear(), resourceChargeRecordDTO.getProjectTotalMonth().getMonthValue(), resourceChargeRecordDTO.getProjectId());
        }
        if (CollectionUtils.isEmpty(resourceChargeRecordVOS)) {
            return resultMap;
        }
        // 转换资源费用使用占比
        return this.getResourcePercent(resultMap, resourceChargeRecordVOS);
    }
}
