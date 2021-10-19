package com.fitmgr.admin.service;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Fitmgr
 * @date ：Created in 2021/1/11 14:17
 * @modified By：
 */
public interface ExportExcelService {
    /**
     * exportRoleMenuExcel
     * 
     * @param response response
     * @throws Exception
     */
    void exportRoleMenuExcel(HttpServletResponse response) throws Exception;

    /**
     * exportRoleFunctionExcel
     * 
     * @param response response
     * @throws Exception
     */
    void exportRoleFunctionExcel(HttpServletResponse response) throws Exception;

    /**
     * exportRoleMenuExcelTest
     * 
     * @param response response
     * @throws Exception
     */
    void exportRoleMenuExcelTest(HttpServletResponse response) throws Exception;

}
