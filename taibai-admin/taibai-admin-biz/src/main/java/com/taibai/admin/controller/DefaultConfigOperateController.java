package com.fitmgr.admin.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fitmgr.admin.api.entity.DefaultConfigOperate;
import com.fitmgr.admin.mapper.DefaultConfigOperateMapper;
import com.fitmgr.common.core.util.R;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/default-config-operate")
public class DefaultConfigOperateController {

    private final DefaultConfigOperateMapper defaultConfigOperateMapper;

    @GetMapping(value = {"/all"})
    public R queryAll() {
        List<DefaultConfigOperate> defaultConfigOperates =  defaultConfigOperateMapper.selectList(new QueryWrapper<>());
        return R.ok(defaultConfigOperates);
    }

    @GetMapping(value = {"/importData"})
    public R importData(){
        FileInputStream is = null;
        Workbook workbook = null;
        try {
            is = new FileInputStream("baseConfig.xlsx");
            workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Cell cell = null;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                cell = row.getCell(2);
                if(cell == null) {
                    break;
                }
                if(StringUtils.isEmpty(cell.getStringCellValue())) {
                    continue;
                }
                DefaultConfigOperate defaultConfigOperate = new DefaultConfigOperate();
                defaultConfigOperate.setFunction(cell.getStringCellValue());
                String httpMethod = row.getCell(3).getStringCellValue();
                if(StringUtils.isEmpty(httpMethod)) {
                    continue;
                }
                defaultConfigOperate.setHttpMethod(httpMethod.trim());
                String httpUrl = row.getCell(4).getStringCellValue();
                if(StringUtils.isEmpty(httpUrl)) {
                    continue;
                }
                defaultConfigOperate.setHttpUrl(httpUrl.trim());
                String isRecord = row.getCell(5).getStringCellValue();
                if("否".equalsIgnoreCase(isRecord.trim())) {
                    continue;
                }

                String primaryIdType = row.getCell(6).getStringCellValue();
                if(StringUtils.isNotEmpty(primaryIdType)) {
                    defaultConfigOperate.setSetIdFlag(true);
                    defaultConfigOperate.setPrimaryIdType(primaryIdType.trim());
                }
                String idKeyName = row.getCell(7).getStringCellValue();
                if(StringUtils.isNotEmpty(idKeyName)) {
                    defaultConfigOperate.setIdKeyName(idKeyName.trim());
                }
                String uuidKeyName = row.getCell(8).getStringCellValue();
                if(StringUtils.isNotEmpty(uuidKeyName)) {
                    defaultConfigOperate.setUuidKeyName(uuidKeyName.trim());
                }
                String uuidType = row.getCell(9).getStringCellValue();
                if(StringUtils.isNotEmpty(uuidType)) {
                    defaultConfigOperate.setUuidType(uuidType.trim());
                }
                String bodyType = row.getCell(10).getStringCellValue();
                if(StringUtils.isNotEmpty(bodyType)) {
                    defaultConfigOperate.setBodyType(bodyType.trim());
                }

                String memberDesc = row.getCell(11).getStringCellValue();
                if(StringUtils.isNotEmpty(memberDesc)) {
                    defaultConfigOperate.setMemberDesc(memberDesc);
                }
                defaultConfigOperateMapper.insert(defaultConfigOperate);
            }
        } catch(Exception  ex) {
            log.error("import fail", ex);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }

            if(workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {

                }
            }
        }
        return R.ok();
    }
}
