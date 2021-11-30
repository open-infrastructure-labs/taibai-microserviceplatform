package com.fitmgr.common.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fitmgr
 * @date 2020/5/2 17:23
 * @Description 正则验证工具类
 */
public class RegularUtil {

    /**
     * 字符串是否包含中文
     * 
     * @param code 唯一编码
     * @return true 包含中文字符 false 不包含中文字符
     */
    public static boolean isContainChinese(String code) {
        Pattern pattern = Pattern.compile("[\u4E00-\u9FA5]");
        Matcher mat = pattern.matcher(code);
        if (mat.find()) {
            return true;
        }
        return false;
    }

    public static boolean isContainEnglish(String code) {
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher mat = pattern.matcher(code);
        if (mat.find()) {
            return true;
        }
        return false;
    }
}
