package com.taibai.admin.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitmgr.admin.service.ExportExcelService;

import lombok.AllArgsConstructor;

/**
 * @author Taibai
 * @date ：Created in 2021/1/11 14:17
 * @modified By：
 */
@RestController
@AllArgsConstructor
@RequestMapping("/excel")
public class ExportExcelController {
    private final ExportExcelService exportExcelService;

    @GetMapping("/test")
    public void exportMenuExcel(HttpServletResponse response) throws Exception {
        exportExcelService.exportRoleMenuExcel(response);
    }

    @GetMapping("/function")
    public void exportFunctionExcel(HttpServletResponse response) throws Exception {
        exportExcelService.exportRoleFunctionExcel(response);
    }

    @GetMapping("/menu")
    public void exportFunctionExcelTest(HttpServletResponse response) throws Exception {
        exportExcelService.exportRoleMenuExcelTest(response);
    }
}
