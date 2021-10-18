package com.fitmgr.meterage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.meterage.api.dto.DiscountItemDTO;
import com.fitmgr.meterage.api.entity.DiscountItem;
import com.fitmgr.meterage.api.vo.DiscountItemVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
public interface IDiscountItemService extends IService<DiscountItem> {

    /**
     * 创建折扣项
     *
     * @param discountItemDTO
     * @return
     */
    boolean createDiscountItem(DiscountItemDTO discountItemDTO);

    /**
     * 更新折扣项
     *
     * @param discountItemDTO
     * @return
     */
    boolean updateDiscountItem(DiscountItemDTO discountItemDTO);

    /**
     * 查询折扣项列表
     * @param page
     * @param discountItemDTO
     * @return
     */
    IPage<DiscountItemVO> selectPage(Page page, DiscountItemDTO discountItemDTO);

    /**
     *  查询折扣项详情
     * @param disCountId
     * @return
     */
    DiscountItemVO getDisCountDetail(String disCountId);


    /**
     * 删除折扣项
     *
     * @param discountItemId
     * @return
     */
    boolean deleteDiscountItem(String discountItemId);

    /**
     * 根据计费项ID查询所有折扣项列表
     * @param chargeItemId
     * @return
     */
    List<DiscountItemVO> getDisCountItemList(String chargeItemId);

    /**
     * 折扣项导出列表
     * @param response
     * @param page
     * @param discountItemDTO
     */
    void exportExcel(HttpServletResponse response, Page page, DiscountItemDTO discountItemDTO);
}
