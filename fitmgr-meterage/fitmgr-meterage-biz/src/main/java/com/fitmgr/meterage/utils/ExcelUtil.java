package com.fitmgr.meterage.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导出工具类
 *
 * @author zhangxiaokang
 * @date 2020/10/30 14:53
 */
public class ExcelUtil<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * @param title   导出excel的文件名字
     * @param headers excel中内容的标题
     * @param fields  xml中配置要显示的属性对象
     * @param dataset 要导出的数据
     * @param out     输出流（文件输出位置）
     * @param pattern 日期的格式
     */
    public void exportExcel(String title, String[] headers, List<FieldSetting> fields, Collection<T> dataset, OutputStream out, String pattern) {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            // 生成表格
            HSSFSheet sheet = workbook.createSheet(title);
            // ----------标题样式
            // 生成一个样式
            final HSSFCellStyle style = workbook.createCellStyle();
            // 设置这些样式 水平布局，居中
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            // 垂直居中;
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            // 生成一个字体
            final HSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 12);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            // 把字体应用到当前样式当中
            style.setFont(font);

            // ----------内容样式
            // 生成并设置另一个样式
            HSSFCellStyle style2 = workbook.createCellStyle();
            //水平布局：居中
            style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //垂直居中
            style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            // 生成另一个字体
            HSSFFont font2 = workbook.createFont();
            font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
            // 把字体应用到当前的样式
            style2.setFont(font2);
            style2.setWrapText(true);
            // 产生表格标题行
            HSSFRow row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style);
                // 设置表头颜色
                style.setFillPattern(HSSFCellStyle.ALIGN_CENTER);
                style.setFillForegroundColor(HSSFColor.AQUA.index);
                style.setFillBackgroundColor(HSSFColor.LIGHT_BLUE.index);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);
                cell.setCellValue(text);
            }
            // 遍历集合数据，产生数据行
            Iterator<T> it = dataset.iterator();
            int index = 0;

            while (it.hasNext()) {
                index++;
                row = sheet.createRow(index);
                final T t = it.next();
                // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
                for (int i = 0; i < fields.size(); i++) {
                    // 创建单元格
                    HSSFCell cell = row.createCell(i);
                    //设置单元格样式
                    cell.setCellStyle(style2);
                    //指定单元格格式：数值、公式或字符串
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    FieldSetting field = fields.get(i);
                    //字段名
                    String fieldName = field.getFieldDbName();
                    //如果设置宽度了，则取配置的值
                    if (field.getFieldWidth() != null && !field.getFieldWidth().equals(0)) {
                        sheet.setColumnWidth(i, field.getFieldWidth() * 256);
                    } else {
                        //设置单元格自适应宽度
                        sheet.autoSizeColumn(i);
                    }
                    //拼接的方法名
                    String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    @SuppressWarnings("rawtypes")
                    Class tCls = t.getClass();
                    @SuppressWarnings("unchecked")
                    Method getMethod = null;

                    getMethod = tCls.getMethod(getMethodName, new Class[]{});
                    Object value = getMethod.invoke(t, new Object[]{});
                    // 判断值的类型后进行强制类型转换
                    String textValue = null;
                    if (value instanceof Date) {
                        Date date = (Date) value;
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        textValue = sdf.format(date);
                    } else {
                        // 其它数据类型都当作字符串简单处理
                        if (value == null) {
                            value = "";
                        }
                        textValue = value.toString();
                    }
                    if (StringUtils.isNotBlank(textValue)) {
                        cell.setCellValue(textValue);
                    }
                }
            }
            out.flush();
            workbook.write(out);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
