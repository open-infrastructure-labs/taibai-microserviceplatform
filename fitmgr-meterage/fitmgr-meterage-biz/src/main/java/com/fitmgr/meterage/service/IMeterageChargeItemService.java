package com.fitmgr.meterage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.api.vo.ChargeItemVO;
import com.fitmgr.resource.api.vo.ComponentVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 计费项-服务类
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
public interface IMeterageChargeItemService extends IService<ChargeItem> {

    /**
     * 创建/添加计费项
     *
     * @param chargeItemDTO
     * @return
     */
    boolean createChargeItem(ChargeItemDTO chargeItemDTO);

    /**
     * 根据计费项uuid，查询计费项详情
     *
     * @param chargeId
     * @return
     */
    ChargeItemVO findChargeItemVOByUuid(String chargeId);

    /**
     * 根据过滤条件查询资源及资源所属的云平台集合
     * @param resourceId
     * @return
     */
    List<MeterageProject> getComponentListByCondition(Integer resourceId);

    /**
     * 修改计费项
     *
     * @param chargeItemDTO
     * @return
     */
    boolean updateChargeItem(ChargeItemDTO chargeItemDTO);

    /**
     * 启用计费项
     *
     * @param chargeItemId
     * @return
     */
    boolean enableChargeItem(String chargeItemId);

    /**
     * 禁用计费项
     *
     * @param chargeItemId
     * @return
     */
    boolean disableChargeItem(String chargeItemId);

    /**
     * 删除计费项
     *
     * @param chargeItemId
     * @return
     */
    boolean deleteChargeItem(String chargeItemId);

    /**
     * 分页查询计费项目信息
     *
     * @param page
     * @param chargeItemDTO
     * @return
     */
    IPage<ChargeItemVO> selectPage(Page page, ChargeItemDTO chargeItemDTO);

    /**
     * 导出选中的计费项
     *
     * @param page
     * @param chargeItemDTO
     */
    void exportExcel(HttpServletResponse response, Page page, ChargeItemDTO chargeItemDTO);

    /**
     * 通过计费项id集合查询计费项集合
     *
     * @param chargeItemIds
     * @return
     */
    List<ChargeItemVO> selectChargeItemListByChargeItemIds(List<String> chargeItemIds);

}
