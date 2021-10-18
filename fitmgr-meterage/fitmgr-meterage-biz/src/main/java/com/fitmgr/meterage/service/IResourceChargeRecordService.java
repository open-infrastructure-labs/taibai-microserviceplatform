package com.fitmgr.meterage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.dto.ResourceChargeRecordDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.api.vo.DiscountItemVO;
import com.fitmgr.meterage.api.vo.ResourceChargeRecordVO;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 账单明细-服务类
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-22
 */
public interface IResourceChargeRecordService extends IService<ResourceChargeRecord> {

    /**
     * 计量记录入库计费账单表
     *
     * @param resourceMeterageRecord
     */
    boolean saveResourceBillDetail(ResourceMeterageRecord resourceMeterageRecord);

    /**
     * 资源下线，统计费用
     *
     * @param resourceMeterageRecord
     * @return
     */
    boolean deleteResourceBillDetail(ResourceMeterageRecord resourceMeterageRecord);

    /**
     * 更新计费记录
     *
     * @param resourceMeterageRecord
     * @return
     */
    boolean updateResourceBillDetail(ResourceMeterageRecord resourceMeterageRecord);

    /**
     * 更新账单记录，结算当前费用
     *
     * @param resourceChargeRecordList
     * @param remark
     */
    void counterCharge(List<ResourceChargeRecord> resourceChargeRecordList, String remark);


    /**
     * 计量记录匹配折扣项->转换位计费详情
     *
     * @param resourceMeterageRecord
     * @param disCountItemList
     * @return
     */
    ResourceChargeRecord getChargeBillDetail(ResourceMeterageRecord resourceMeterageRecord, List<DiscountItemVO> disCountItemList, ChargeItemDTO chargeItemDTO);

    /**
     * 计算时长
     *
     * @param chargeItem
     * @param startTime
     * @param finishedTime
     * @return
     */
    Long accountTime(ChargeItem chargeItem, LocalDateTime startTime, LocalDateTime finishedTime);

    /**
     * 获取时长时间
     * @param chargeItem
     * @param startTime
     * @param finishedTime
     * @return
     */
    Long getTime(ChargeItem chargeItem, LocalDateTime startTime, LocalDateTime finishedTime);
    /**
     * 根据条件分页查询账单明细数据
     *
     * @param page
     * @param resourceChargeRecordDTO
     * @return
     */
    IPage<ResourceChargeRecordVO> selectPage(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 根据cmpInstanceName集合查询账单记录集合--启用的资源
     *
     * @param cmpInstanceList
     * @return
     */
    List<ResourceChargeRecord> selectChargeBillDetailListByCmpInstanceNameList(List<String> cmpInstanceList);

    /**
     * 根据cmpInstanceName集合查询账单记录集合--启用的资源
     *
     * @param cmpInstanceList
     * @return
     */
    List<ResourceChargeRecord> selectChargeDisableBilllList(List<String> cmpInstanceList);



    /**
     * 根据计费项Id，查询账单记录-->未结算资源
     *
     * @param chargeItemId
     * @return
     */
    List<ResourceChargeRecord> selectChargeBillDetailListByChargeItemId(String chargeItemId);

    /**
     * 根据计费项Id，查询账单记录-->之前禁用计费项的资源
     *
     * @param chargeItemId
     * @return
     */
    List<ResourceChargeRecord> selectAccountChargeBillDetailListByChargeItemId(String chargeItemId);

    /**
     * 新增账单记录
     *
     * @param billDetails
     * @param chargeItemDTO
     */
    void insertChargeBill(List<ResourceChargeRecord> billDetails, ChargeItemDTO chargeItemDTO);

    /**
     * 更新账单记录
     *
     * @param billDetails
     */
//    void accountCharge(List<ResourceChargeRecord> billDetails, String remark);

    /**
     * 月度账单汇总统计
     *
     * @return
     */
    boolean totalChargeBillRecord();

    /**
     * 导出选择数据-->账单记录
     *
     * @param page
     * @param resourceChargeRecordDTO
     */
    void exportChargeBillDetail(HttpServletResponse response, Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 按月统计租户费用
     *
     * @param page
     * @param resourceChargeRecordDTO
     * @return
     */
    IPage<ResourceChargeRecordVO> totalTenantMonth(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 按年/半年统计租户费用
     *
     * @param page
     * @param resourceChargeRecordDTO
     * @return
     */
    IPage<ResourceChargeRecordVO> totalTenantYear(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 按月统计Project费用
     *
     * @param page
     * @param resourceChargeRecordDTO
     * @return
     */
    IPage<ResourceChargeRecordVO> totalProjectMonth(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 按年统计Project费用
     *
     * @param page
     * @param resourceChargeRecordDTO
     * @return
     */
    IPage<ResourceChargeRecordVO> totalProjectYear(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 所有租户总费用及产品占比
     *
     * @return
     */
    Map<String, Map<String, String>> totalTenantPrice();

    /**
     * 近半年或一年某个租户费用趋势
     *
     * @return
     */
    List<ResourceChargeRecordVO> reviewTenantPrice(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 近半年或一年某个Project费用趋势
     *
     * @return
     */
    List<ResourceChargeRecordVO> reviewProjectPrice(Page page, ResourceChargeRecordDTO resourceChargeRecordDTO);

    /**
     * 某个月某个租户的费用占比
     *
     * @return
     */
    Map<String, Map<String, String>> reviewTotalPrice(ResourceChargeRecordDTO resourceChargeRecordDTO);
}
