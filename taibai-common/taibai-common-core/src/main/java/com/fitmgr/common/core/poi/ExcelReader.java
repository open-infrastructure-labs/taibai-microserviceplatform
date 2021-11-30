package com.fitmgr.common.core.poi;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * <类描述> Excel解析工具类
 * 
 * @Author: KironLiu
 * @Date: 2020/8/18 15:14
 */
@Slf4j
@Component
public class ExcelReader {

    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";
    private static final String SPOT = ".";

    /**
     * 根据文件后缀名类型获取对应的工作簿对象
     * 
     * @param inputStream 读取文件的输入流
     * @param fileType    文件后缀名类型（xls或xlsx）
     * @return 包含文件数据的工作簿对象
     * @throws IOException
     */
    public static Workbook getWorkbook(InputStream inputStream, String fileType) throws IOException {
        Workbook workbook = null;
        if (fileType.equalsIgnoreCase(XLS)) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (fileType.equalsIgnoreCase(XLSX)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        return workbook;
    }

    /**
     * 读取Excel文件内容
     * 
     * @return 读取结果列表，读取失败时返回null
     */
    public static List<Map<String, Object>> readExcel(MultipartFile mFile) {
        Workbook workbook = null;
        try {
            // 获取workBook
            // 获取Excel文件名
            String fileName = mFile.getOriginalFilename();
            if (fileName == null || fileName.isEmpty() || fileName.lastIndexOf(SPOT) < 0) {
                log.warn("analysis Excel failure because the filename suffix is illegal,file name ={}", fileName);
                return null;
            }
            // 获取Excel文件类型（.xls/.xlsx）
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            // 获取Excel工作簿
            workbook = getWorkbook(mFile.getInputStream(), fileType);
            if (ObjectUtils.isEmpty(workbook)) {
                log.info("get workbook fail,the workbook is null!");
            }
            // TODO 读取excel中的数据
            List<Map<String, Object>> resultDataList = parseExcel(workbook);
            log.info("解析返回数据：{}", resultDataList);
            return resultDataList;
        } catch (Exception e) {
            String fileName = mFile.getOriginalFilename();
            log.warn("analysis Excel failure，fileName：{} error info：{}", fileName, e.getMessage());
            throw new IllegalArgumentException("Excel文件解析失败，请检查文件是否正确!");
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
            } catch (Exception e) {
                log.warn("Closing workbook Error！error info：" + e.getMessage());
            }
        }
    }

    /**
     * 解析Excel数据
     * 
     * @param workbook Excel工作簿对象
     * @return 解析结果
     */
    private static List<Map<String, Object>> parseExcel(Workbook workbook) {
        List<Map<String, Object>> resultDataList = new ArrayList<>();
        // 解析sheet
        log.info("解析Excel文件，sheet数量：{}", workbook.getNumberOfSheets());
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            Sheet sheet = workbook.getSheetAt(sheetNum);
            // 校验sheet是否合法
            if (sheet == null) {
                log.info("sheet{}未空", sheetNum + 1);
                continue;
            }
            // 获取sheet的第一行
            log.info("解析sheet{}数据", sheetNum + 1);
            int firstRowNum = sheet.getFirstRowNum();
            Row firstRow = sheet.getRow(firstRowNum);
            log.info("解析sheet{}表头", sheetNum + 1);
            if (null == firstRow) {
                log.warn("解析Excel失败，在第一行没有读取到任何数据！");
                continue;
            }
            // 获取表头数据列表-->即对应数据库字段名列表
            List<String> nameList = getTopNameList(firstRow);
            short cellNum = firstRow.getLastCellNum();
            log.info("****************** Excel表共有{}列", cellNum);
            // 解析每一行的数据，构造数据对象
            log.info("跳过字段名和注释，从当前sheet的第三行开始读取数据");
            int rowStart = firstRowNum + 2;
            // 获取实际行数
            int rowEnd = sheet.getPhysicalNumberOfRows();
            // 遍历单张sheet表格，处理每行数据
            for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
                // 获取当前sheet的rowNum行数据 row存储行数据
                Row row = sheet.getRow(rowNum);
                if (null == row || isRowEmpty(row)) {
                    continue;
                }
                // 获取单行数据
                Map<String, Object> singleRowData = convertRowToData(row, nameList);
                if (null == singleRowData || singleRowData.isEmpty()) {
                    log.warn("第 " + row.getRowNum() + "行数据不合法，已忽略！");
                    continue;
                }
                log.info("第{}行数据为：{},将数据放入集合中", rowNum + 1, singleRowData);
                resultDataList.add(singleRowData);
            }
        }
        log.info("读取结束，数据为：{}", resultDataList);
        return resultDataList;
    }

    /**
     * 判断行是否为空(仅有格式也算空)
     * 
     * @param row
     * @return
     */
    public static boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (null == cell) {
                continue;
            }
            Object cellValue = getCellValue(cell);
            if (null != cellValue && String.valueOf(cellValue).length() != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将单元格内容转换为字符串
     * 
     * @param cell
     * @return
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        Object returnValue = null;
        switch (cell.getCellType()) {
        //// 数字
        case NUMERIC:
            switch (cell.getCellStyle().getDataFormatString()) {
            case "yyyy/m/d\\ h:mm;@":
                returnValue = LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneId.systemDefault());
                break;
            default:
                double temp = cell.getNumericCellValue();
                int tempint = (int) temp;
                if (temp % tempint == 0) {
                    returnValue = tempint;
                } else {
                    returnValue = temp;
                }
            }
            break;
        // 字符串
        case STRING:
            returnValue = cell.getStringCellValue();
            break;
        // 布尔
        case BOOLEAN:
            returnValue = cell.getBooleanCellValue();
            break;
        // 空值
        case BLANK:
            break;
        // 公式
        case FORMULA:
            returnValue = cell.getCellFormula();
            break;
        // 故障
        case ERROR:
            break;
        default:
            break;
        }
        return returnValue;
    }

    /**
     * 提取每一行中需要的数据，构造成为一个结果数据对象
     *
     * 当该行中有单元格的数据为空或不合法时，忽略该行的数据
     *
     * @param row         行数据
     * @param topNameList 表头行数据集合
     * @return 解析后的行数据对象，行数据错误时返回null
     */
    private static Map<String, Object> convertRowToData(Row row, List<String> topNameList) {
        Map<String, Object> singleRowData = new HashMap<>(16);
        Cell cell;
        // 处理Excel单元格数据，给实体类对象属性赋值
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            cell = row.getCell(cellNum);
            // 获取单元格数据
            String fieldName = topNameList.get(cellNum);
            Object cellValue = getCellValue(cell);
            if (null == cellValue || 0 == cellValue.toString().trim().length()) {
                cellValue = "";
            }
            singleRowData.put(fieldName, cellValue);
        }
        return singleRowData;
    }

    /**
     * 获取Excel表头信息
     * 
     * @param firstRow
     * @return
     */
    public static List<String> getTopNameList(Row firstRow) {
        ArrayList<String> topNameList = new ArrayList<>();
        Cell cell;
        for (int cellNum = 0; cellNum < firstRow.getLastCellNum(); cellNum++) {
            cell = firstRow.getCell(cellNum);
            String target = convertCellValueToString(cell);
            topNameList.add(target);
        }
        return topNameList;
    }

    private static String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
        case NUMERIC:
            // 数字
            Double doubleValue = cell.getNumericCellValue();
            // 格式化科学计数法，取一位整数
            DecimalFormat df = new DecimalFormat("0");
            returnValue = df.format(doubleValue);
            break;
        case STRING:
            // 字符串
            returnValue = cell.getStringCellValue();
            break;
        case BOOLEAN:
            // 布尔
            Boolean booleanValue = cell.getBooleanCellValue();
            returnValue = booleanValue.toString();
            break;
        case BLANK:
            // 空值
            break;
        case FORMULA:
            // 公式
            returnValue = cell.getCellFormula();
            break;
        case ERROR:
            // 故障
            break;
        default:
            break;
        }
        return returnValue;
    }
}
