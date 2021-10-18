package com.fitmgr.meterage.utils;

import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 时间转换类
 *
 * @author zhangxiaokang
 * @date 2020/10/27 17:24
 */
public class DateTimeConvertUtil {

    /**
     * 时数差，向上取整
     * @param startTiem
     * @param endTime
     * @return
     */
    public static long hourDiff(LocalDateTime startTiem, LocalDateTime endTime) {
        return ChronoUnit.HOURS.between(startTiem, endTime) + 1;
    }

    /**
     * 天数差，向上取整
     * @param startTiem
     * @param endTime
     * @return
     */
    public static long dayDiff(LocalDateTime startTiem, LocalDateTime endTime) {
        return ChronoUnit.DAYS.between(startTiem, endTime) + 1;
    }

    /**
     * 月数差，向上取整
     * @param startTiem
     * @param endTime
     * @return
     */
    public static long monthDiff(LocalDateTime startTiem, LocalDateTime endTime) {
        return ChronoUnit.MONTHS.between(startTiem, endTime) + 1;
    }

    /**
     * 年数差，向上取整
     * @param startTiem
     * @param endTime
     * @return
     */
    public static long yearDiff(LocalDateTime startTiem, LocalDateTime endTime) {
        return ChronoUnit.YEARS.between(startTiem, endTime) + 1;
    }

    /**
     * 当前时间+N小时
     * @param nowDateTime
     * @param n
     * @return
     */
    public static LocalDateTime addOneHours(LocalDateTime nowDateTime, Long n) {
        return nowDateTime.plusHours(n);
    }

    /**
     * 当前时间+N天
     * @param nowDateTime
     * @param n
     * @return
     */
    public static LocalDateTime addOneDays(LocalDateTime nowDateTime, Long n) {
        return nowDateTime.plusDays(n);
    }

    /**
     * 当前时间+N个月
     * @param nowDateTime
     * @param n
     * @return
     */
    public static LocalDateTime addOneMonth(LocalDateTime nowDateTime, Long n) {
        return nowDateTime.plusMonths(n);
    }

    /**
     * 当前时间+N年
     * @param nowDateTime
     * @param n
     * @return
     */
    public static LocalDateTime addOneYears(LocalDateTime nowDateTime, Long n) {
        return nowDateTime.plusYears(n);
    }

    /**
     * 根据code计算未来时间差
     * @param nowDateTime
     * @param n
     * @param code
     * @return
     */
    public static LocalDateTime calculateDateTime(LocalDateTime nowDateTime, Long n, Integer code) {
        switch (code) {
            case 1:
                return DateTimeConvertUtil.addOneHours(nowDateTime, n);
            case 2:
                return DateTimeConvertUtil.addOneDays(nowDateTime, n);
            case 3:
                return DateTimeConvertUtil.addOneMonth(nowDateTime, n);
            case 5:
                return DateTimeConvertUtil.addOneYears(nowDateTime, n);
            default:
                throw new BusinessException(BusinessEnum.PARAMETER_FAULT);
        }
    }

    public static String getTimeStr(LocalDateTime localDateTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return df.format(localDateTime);
    }

    public static String getBillCycleTimeStr(LocalDateTime localDateTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM");
        return df.format(localDateTime);
    }
}
