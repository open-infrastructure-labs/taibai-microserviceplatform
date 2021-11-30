package com.taibai.admin.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 日期工具类
 *
 * @author Taibai
 * @date: 2020年10月27日 下午3:42:19
 */
@Slf4j
public class DateUtils {

    /**
     * 默认时间字符串的格式
     */
    public static final String DEFAULT_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT_STR = "yyyyMMdd";

    public static final String DAY_FORMAT_STR = "yyyy-MM-dd";

    public static final String DAY_FORMAT_STR_2 = "yyyy.MM.dd";

    public static final String DAY_FORMAT_STR_3 = "MMdd";

    public static final String CALENDAR_FORMAT_STR = "yyyy年MM月dd日";

    public static final String TIME_FORMAT_STR = "HHmmss";

    /**
     * 
     * @methodName: isPass
     * @description: 判断指定时间是否已经过去
     * @param time
     * @return
     */
    public static boolean isPass(String time) {
        if (StringUtils.isEmpty(time))
            return false;
        Date now;
        String nowStr = getCurrentTime(DEFAULT_FORMAT_STR);
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_FORMAT_STR);
        try {
            Date desTime = format.parse(time);
            now = format.parse(nowStr);
            if (now.getTime() - desTime.getTime() >= 0) {
                return true;
            }
        } catch (ParseException e) {
            log.error("ParseException!", e);
        }

        return false;
    }

    /**
     * 
     * @methodName: isPass
     * @description: 判断是否符合now时间n天后的时间之内
     * @param now
     * @param date
     * @param n
     * @return
     */
    public static boolean isPass(Date now, Date date, int n) {
        Date nDay = DateUtils.nextSpecifiednDayTime(date, n);
        if (n > 0) {
            return DateUtils.isBetween(now, date, nDay);
        }

        return DateUtils.isBetween(now, nDay, date);
    }

    /**
     * 
     * @methodName: getCurrentTime
     * @description: 获取当前时间
     * @return
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 
     * @methodName: getCurrentTime
     * @description: 获取当前时间
     * @param formatStr
     * @return
     */
    public static String getCurrentTime(String formatStr) {
        if (StringUtils.isEmpty(formatStr)) {
            formatStr = DEFAULT_FORMAT_STR;
        }
        return date2String(new Date(), formatStr);
    }

    /**
     * 
     * @methodName: getTodayDate
     * @description: 获取当天凌晨0点的时间
     * @return
     */
    public static Date getTodayDate() {
        String today = DateUtils.getCurrentTime(DateUtils.DAY_FORMAT_STR);
        Date date = DateUtils.string2Date(today, DateUtils.DAY_FORMAT_STR);
        return date;
    }

    /**
     * 
     * @methodName: getTodayEndTime
     * @description: 获取当天23:59:59的时间
     * @return
     */
    public static Date getTodayEndTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    public static void main(String[] args) {
        System.out.println(getMonthBeginDay());
    }

    /**
     * 
     * @methodName: getMonthBeginOrEnd
     * @description: 获取指定日期当月第一天或者最后一天
     * @param dateStr
     * @param day     1：第一天，-1：最后一天
     * @param format
     * @return
     */
    public static String getMonthBeginOrEnd(String dateStr, int day, String format) {
        Date date = string2Date(dateStr, format);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (day == -1) {
            c.add(Calendar.MONTH, 1);
            c.set(Calendar.DAY_OF_MONTH, 0);
        } else if (day == 1) {
            c.add(Calendar.MONTH, 0);
            c.set(Calendar.DAY_OF_MONTH, 1);
        }

        return date2String(c.getTime(), format);
    }

    /**
     * 
     * @methodName: getMonthBeginOrEnd
     * @description: 获取指定日期当月第一天或者最后一天
     * @param dateStr
     * @param day     1：第一天，-1：最后一天
     * @param format
     * @return
     */
    public static Date getMonthBeginOrEnd(Date date, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (day == -1) {
            c.add(Calendar.MONTH, 1);
            c.set(Calendar.DAY_OF_MONTH, 0);
        } else if (day == 1) {
            c.add(Calendar.MONTH, 0);
            c.set(Calendar.DAY_OF_MONTH, 1);
        }

        return c.getTime();
    }

    /**
     * 
     * @methodName: getMonthBeginDay
     * @description: 获取当月第一天时间
     * @return
     */
    public static Date getMonthBeginDay() {
        return getWeeHoursTime(getMonthBeginOrEnd(new Date(), 1));
    }

    /**
     * 
     * @methodName: getTodayChar
     * @description: 返回年月日
     * @param dateFormat
     * @return
     */
    public static String getTodayChar(String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(new Date());
    }

    /**
     * 
     * @methodName: getWeeHoursTime
     * @description: 获取指定日期凌晨0点时间
     * @param date
     * @return
     */
    public static Date getWeeHoursTime(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 
     * @methodName: getMonthTime
     * @description: 获取指定月份时间
     * @param date
     * @return
     */
    public static Date getMonthTime(int month) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.MONTH, month - 1);
        return c.getTime();
    }

    /**
     * 
     * @methodName: date2String
     * @description: 将Date日期转换为String
     * @param date
     * @param formatStr
     * @return
     */
    public static String date2String(Date date, String formatStr) {
        if (null == date) {
            date = new Date();
        }
        if (null == formatStr) {
            formatStr = DEFAULT_FORMAT_STR;
        }
        SimpleDateFormat df = new SimpleDateFormat(formatStr);

        return df.format(date);
    }

    /**
     * 
     * @methodName: string2Date
     * @description: 将String转换为Date
     * @param dateStr
     * @param formatStr
     * @return
     */
    public static Date string2Date(String dateStr, String formatStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        if (StringUtils.isEmpty(formatStr)) {
            formatStr = DEFAULT_FORMAT_STR;
        }
        SimpleDateFormat df = new SimpleDateFormat(formatStr);
        Date date = null;

        try {
            date = df.parse(dateStr);
        } catch (ParseException e) {
            return date;
        }

        return date;
    }

    /**
     * 
     * @methodName: nextNDay
     * @description: 获取第N天后的日期
     * @param day：负数表示day天之前
     * @return
     */
    public static String nextnDay(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        String nextnDay = new SimpleDateFormat(DAY_FORMAT_STR).format(cal.getTime());

        return nextnDay;
    }

    /**
     * 
     * @methodName: nextSpecifiedNDay
     * @description: 获取指定日期第N天后的日期（日期类型）
     * @param date
     * @param day
     * @return
     */
    public static Date nextSpecifiednDayTime(Date date, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, day);

        return cal.getTime();
    }

    /**
     * 
     * @methodName: nextSpecifiedNDay
     * @description: 获取指定日期第N天后的日期
     * @param date
     * @param day
     * @return
     */
    public static String nextSpecifiednDay(Date date, int day) {
        Date nextDate = DateUtils.nextSpecifiednDayTime(date, day);
        return DateUtils.date2String(nextDate, DateUtils.DEFAULT_FORMAT_STR);
    }

    /**
     * 
     * @methodName: getNextMonth
     * @description: 获取n月前或月后的日期
     * @param date
     * @param year
     * @return
     */
    public static Date getNextMonth(Date date, int month) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, month);
        return cal.getTime();
    }

    /**
     * 
     * @methodName: getNextYears
     * @description: 获取n年前或年后的日期
     * @param date
     * @param year
     * @return
     */
    public static Date getNextYears(Date date, int year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, year);
        return cal.getTime();
    }

    /**
     * 
     * @methodName: isBetween
     * @description: 判断给定时间在否在给定两个时间之前
     * @param star
     * @param end
     * @return
     */
    public static boolean isBetween(Date date, Date start, Date end) {
        try {
            if (date == null) {
                return false;
            }
            if (date.getTime() >= start.getTime() && date.getTime() <= end.getTime()) {
                return true;
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }

        return false;
    }

    /**
     * 
     * @methodName: getHours
     * @description:
     * @return
     */
    public static int getHours() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 
     * @methodName: getYear
     * @description:
     * @return
     */
    public static int getYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 
     * @methodName: getNexYear
     * @description:
     * @return
     */
    public static int getNexYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + 1;
    }

    /**
     * 
     * @methodName: getMonth
     * @description: TODO(这里用一句话描述这个方法的作用)
     * @return
     */
    public static int getMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 
     * @methodName: getMonth
     * @description: 获取第n个月
     * @return
     */
    public static int getNextnMonth(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, n);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 
     * @methodName: getMonth
     * @description: TODO(这里用一句话描述这个方法的作用)
     * @param date
     * @return
     */
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 输入事件段解析
     * 
     * @param period period
     * @return Long[]
     * @throws ParseException
     */
    public static Long[] periodParse(String period) throws ParseException {
        if (period != null) {
            String[] startAndEnd = period.split("#");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (startAndEnd.length == 2) {
                Long[] times = new Long[2];
                times[0] = sdf.parse(startAndEnd[0]).getTime() / 1000L;
                times[1] = sdf.parse(startAndEnd[1]).getTime() / 1000L;
                return times;
            }
        }
        return null;
    }

    public static Long timeParse(String time) throws ParseException {
        if (StringUtils.isBlank(time))
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(time).getTime();
    }

    public static String dateFarmat(Date date) {
        if (null == date)
            return null;
        // 小写的mm表示的是分钟
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    /**
     * 
     * @param 要转换的毫秒数
     * @return 该毫秒数转换为 * hours * minutes * seconds 后的格式
     */
    public static String formatDuring(long mss) {
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return hours + "小时" + minutes + "分" + seconds + "秒";
    }

    public static long secondsEndOfCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis() / 1000;
    }

    public static Date addYears(final Date date, final int amount) {
        return add(date, Calendar.YEAR, amount);
    }

    public static Date addMonths(final Date date, final int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    public static Date addDays(final Date date, final int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    public static Date addHours(final Date date, final int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    public static Date addMinutes(final Date date, final int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    public static Date addSeconds(final Date date, final int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    private static Date add(final Date date, final int calendarField, final int amount) {
        if (date == null) {
            return null;
        }
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
