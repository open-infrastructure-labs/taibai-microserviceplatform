package com.fitmgr.meterage.mapper;

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.dto.ProjectDTO;
import com.fitmgr.admin.api.vo.ProjectVO;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.api.vo.ResourceChargeRecordVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-22
 */
public interface ResourceChargeRecordMapper extends BaseMapper<ResourceChargeRecord> {

    /**
     * 批量新增账账单信息
     *
     * @param resourceChargeRecords
     * @return
     */
    int saveChargeBillDetailList(@Param("list") List<ResourceChargeRecord> resourceChargeRecords);

    /**
     * 租户月度统计费用
     *
     * @param page
     * @param year
     * @param month
     * @return
     */
    IPage<ResourceChargeRecordVO> listTenantMonthCondition(@Param("page") Page page, @Param("year") Integer year, @Param("month") Integer month);

    /**
     * 租户半年/一年统计费用
     *
     * @param page
     * @param startCount
     * @param endCount
     * @return
     */
    @SqlParser(filter = true)
    @Select({"SELECT\n" +
            "mm.totalCount AS totalCount,\n" +
            "mm.tenantId AS tenantId \n" +
            "FROM\n" +
            "(\n" +
            "SELECT\n" +
            "rm.tenant_id AS tenantId,\n" +
            "SUM( IFNULL(rm.total_charge,0) ) AS totalCount \n" +
            "FROM\n" +
            "( SELECT rc.*, DATE_FORMAT( rc.bill_cycle_time, '%Y-%m' ) AS billCycleTime FROM resource_charge_record  AS rc WHERE rc.tenant_id is NOT NULL ) AS rm \n" +
            "WHERE\n" +
            "rm.billCycleTime IN (\n" +
            "SELECT\n" +
            "m.mont AS mt \n" +
            "FROM\n" +
            "(\n" +
            "SELECT\n" +
            "DATE_FORMAT( TheDate, '%Y-%m' ) AS mont \n" +
            "FROM\n" +
            "(\n" +
            "SELECT\n" +
            "ADDDATE( CURDATE( ), INTERVAL @d MONTH ) AS TheDate,\n" +
            "@d := @d - 1 MONTH \n" +
            "FROM\n" +
            "resource_charge_record,\n" +
            "( SELECT @d := 0 ) temp \n" +
            ") test \n" +
            "LIMIT #{startCount}, #{endCount}) AS m)\t\n" +
            "GROUP BY\n" +
            "rm.tenant_id \n" +
            ") AS mm  \n" +
            "ORDER BY\n" +
            "mm.totalCount DESC"})
    IPage<ResourceChargeRecordVO> listTenantYearCondition(Page page, @Param("startCount") Integer startCount, @Param("endCount") Integer endCount);

    /**
     * Project月度统计费用
     *
     * @param page
     * @param year
     * @param month
     * @return
     */

    IPage<ResourceChargeRecordVO> listProjectMonthCondition(@Param("page") Page page, @Param("year") Integer year, @Param("month") Integer month);

    /**
     * Project半年/一年统计费用
     *
     * @param page
     * @param year
     * @param month
     * @return
     */
    @SqlParser(filter = true)
    @Select({"SELECT\n" +
            "          rm.project_id as projectId,\n" +
            "          SUM(rm.total_charge) as totalCount\n" +
            "        FROM (SELECT\n" +
            "                rc.*,\n" +
            "                DATE_FORMAT(rc.bill_cycle_time, '%Y-%m') AS billCycleTime\n" +
            "              FROM resource_charge_record AS rc) AS rm\n" +
            "        WHERE rm.billCycleTime IN(\n" +
            "                    SELECT m.mont AS mt FROM (SELECT\n" +
            "                        DATE_FORMAT(TheDate, '%Y-%m') AS mont\n" +
            "                        FROM\n" +
            "                        (SELECT\n" +
            "                        ADDDATE(CURDATE(), INTERVAL @d MONTH) AS TheDate,\n" +
            "                        @d := @d - 1\n" +
            "                        MONTH\n" +
            "                        FROM\n" +
            "                        resource_charge_record,\n" +
            "                        (SELECT\n" +
            "                        @d := 0) temp) test\n" +
            "                        LIMIT #{startCount}, #{endCount}) AS m)\n" +
            "        AND rm.project_id IS NOT NULL \n" +
            "        GROUP BY rm.project_id\n" +
            "        ORDER BY totalCount DESC"})
    IPage<ResourceChargeRecordVO> listProjectYearCondition(Page page, @Param("startCount") Integer year, @Param("endCount") Integer month);

    /**
     * 查询所有所有租户的所有费用及其占比
     *
     * @return
     */
    List<ResourceChargeRecordVO> totalTenantPrice();


    /**
     * 某租户近半年/一年费用统计
     *
     * @param startCount
     * @param endCount
     * @param tenantId
     * @return
     */
    @SqlParser(filter = true)
    @Select({"SELECT\n" +
            "          rm.tenant_id AS tenantId,\n" +
            "          rm.billCycleTime AS cycleTime,\n" +
            "          SUM(rm.total_charge) AS totalCount\n" +
            "        FROM (SELECT\n" +
            "                rc.*,\n" +
            "                DATE_FORMAT(rc.bill_cycle_time, '%Y-%m') AS billCycleTime\n" +
            "              FROM resource_charge_record AS rc) AS rm\n" +
            "        WHERE rm.billCycleTime IN(\n" +
            "                    SELECT m.mont AS mt FROM (SELECT\n" +
            "                        DATE_FORMAT(TheDate, '%Y-%m') AS mont\n" +
            "                        FROM\n" +
            "                        (SELECT\n" +
            "                        ADDDATE(CURDATE(), INTERVAL @d MONTH) AS TheDate,\n" +
            "                        @d := @d - 1\n" +
            "                        MONTH\n" +
            "                        FROM\n" +
            "                        resource_charge_record,\n" +
            "                        (SELECT\n" +
            "                        @d := 0) temp) test\n" +
            "                        LIMIT #{startCount}, #{endCount}) AS m)\n" +
            "        AND rm.tenant_id = #{tenantId}\n" +
            "        GROUP BY rm.billCycleTime\n" +
            "        ORDER BY rm.billCycleTime DESC"})
    List<ResourceChargeRecordVO> listReviewTenantMonthCondition(@Param("startCount") Integer startCount,
                                                                @Param("endCount") Integer endCount,
                                                                @Param("tenantId") Integer tenantId);

    /**
     * 某Project半年/一年统计费用
     *
     * @param startCount
     * @param endCount
     * @param projectId
     * @return
     */
    @SqlParser(filter = true)
    @Select({"SELECT\n" +
            "          rm.project_id AS projectId,\n" +
            "          rm.billCycleTime AS cycleTime,\n" +
            "          SUM(rm.total_charge) AS totalCount\n" +
            "        FROM (SELECT\n" +
            "                rc.*,\n" +
            "                DATE_FORMAT(rc.bill_cycle_time, '%Y-%m') AS billCycleTime\n" +
            "              FROM resource_charge_record AS rc) AS rm\n" +
            "        WHERE rm.billCycleTime IN(\n" +
            "                    SELECT m.mont AS mt FROM (SELECT\n" +
            "                        DATE_FORMAT(TheDate, '%Y-%m') AS mont\n" +
            "                        FROM\n" +
            "                        (SELECT\n" +
            "                        ADDDATE(CURDATE(), INTERVAL @d MONTH) AS TheDate,\n" +
            "                        @d := @d - 1\n" +
            "                        MONTH\n" +
            "                        FROM\n" +
            "                        resource_charge_record,\n" +
            "                        (SELECT\n" +
            "                        @d := 0) temp) test\n" +
            "                        LIMIT #{startCount}, #{endCount}) AS m)\n" +
            "        AND rm.project_id = #{projectId}\n" +
            "        GROUP BY rm.billCycleTime\n" +
            "        ORDER BY rm.billCycleTime DESC"})
    List<ResourceChargeRecordVO> listReviewProjectMonthCondition(@Param("startCount") Integer startCount,
                                                                 @Param("endCount") Integer endCount,
                                                                 @Param("projectId") Integer projectId);

    /**
     * 租户月度账单分布汇总
     *
     * @param year
     * @param month
     * @param tenantId
     * @return
     */
    List<ResourceChargeRecordVO> tenantMonthPrice(@Param("year") Integer year,
                                                  @Param("month") Integer month,
                                                  @Param("tenantId") Integer tenantId);

    /**
     * Project月度账单分布汇总
     *
     * @param year
     * @param month
     * @param projectId
     * @return
     */
    List<ResourceChargeRecordVO> projectMonthPrice(@Param("year") Integer year,
                                                   @Param("month") Integer month,
                                                   @Param("projectId") Integer projectId);

}
