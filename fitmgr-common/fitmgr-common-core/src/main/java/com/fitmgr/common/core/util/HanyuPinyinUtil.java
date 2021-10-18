package com.fitmgr.common.core.util;

import lombok.experimental.UtilityClass;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 汉语转换成拼音类
 * 
 * @author Fitmgr
 * @date 2020-01-03
 */
@UtilityClass
public class HanyuPinyinUtil {

    private static final String CHINESE = "[\u4e00-\u9fa5]+";
    private static final String NUMBER = "[0-9]+";
    private static final String LETTER = "[a-zA-Z]+";

    /**
     * 将文字转为汉语拼音
     * 
     * @param ChineseLanguage 要转成拼音的中文
     */
    public String toHanyuPinyin(String chineseLanguage) {
        char[] clChars = chineseLanguage.trim().toCharArray();
        StringBuffer hanyupinyin = new StringBuffer();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出拼音全部小写
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 不带声调
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        try {
            for (int i = 0; i < clChars.length; i++) {
                if (String.valueOf(clChars[i]).matches(CHINESE)) {
                    // 如果字符是中文,则将中文转为汉语拼音
                    hanyupinyin.append(PinyinHelper.toHanyuPinyinStringArray(clChars[i], defaultFormat)[0]);
                } else {
                    // 如果字符不是中文,则不转换
                    hanyupinyin.append(clChars[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
        }
        return hanyupinyin.toString();
    }

    /**
     * 转换拼音字符串中第一个为大写
     * 
     * @param ChineseLanguage
     * @return
     */
    public String getFirstLettersUp(String chineseLanguage) {
        return getFirstLetters(chineseLanguage, HanyuPinyinCaseType.UPPERCASE);
    }

    /**
     * 转换拼音字符串第一个为小写
     * 
     * @param ChineseLanguage
     * @return
     */
    public String getFirstLettersLo(String chineseLanguage) {
        return getFirstLetters(chineseLanguage, HanyuPinyinCaseType.LOWERCASE);
    }

    /**
     * 获取第一个位置
     * 
     * @param ChineseLanguage
     * @param caseType
     * @return
     */
    public String getFirstLetters(String chineseLanguage, HanyuPinyinCaseType caseType) {
        char[] clChars = chineseLanguage.trim().toCharArray();
        StringBuffer hanyupinyin = new StringBuffer();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出拼音全部大写
        defaultFormat.setCaseType(caseType);
        // 不带声调
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        try {
            for (int i = 0; i < clChars.length; i++) {
                String str = String.valueOf(clChars[i]);
                if (str.matches(CHINESE)) {
                    // 如果字符是中文,则将中文转为汉语拼音,并取第一个字母
                    hanyupinyin.append(
                            PinyinHelper.toHanyuPinyinStringArray(clChars[i], defaultFormat)[0].substring(0, 1));
                } else {
                    // 否则不转换
                    // 如果是标点符号的话，带着
                    hanyupinyin.append(clChars[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
        }
        return hanyupinyin.toString();
    }

    /**
     * 获取拼音字符串
     * 
     * @param ChineseLanguage
     * @return
     */
    public String getPinyinString(String chineseLanguage) {
        char[] clChars = chineseLanguage.trim().toCharArray();
        StringBuffer hanyupinyin = new StringBuffer();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出拼音全部大写
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 不带声调
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        try {
            for (int i = 0; i < clChars.length; i++) {
                String str = String.valueOf(clChars[i]);
                if (str.matches(CHINESE)) {
                    // 如果字符是中文,则将中文转为汉语拼音,并取第一个字母
                    hanyupinyin.append(PinyinHelper.toHanyuPinyinStringArray(clChars[i], defaultFormat)[0]);
                } else if (str.matches(NUMBER)) {
                    // 如果字符是数字,取数字
                    hanyupinyin.append(clChars[i]);
                } else if (str.matches(LETTER)) {
                    // 如果字符是字母,取字母
                    hanyupinyin.append(clChars[i]);
                } else {
                    // 否则不转换
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
        }
        return hanyupinyin.toString();
    }

    /**
     * 取第一个汉字的第一个字符 @Title: getFirstLetter @Description: TODO @return String @throws
     */
    public String getFirstLetter(String chineseLanguage) {
        char[] clChars = chineseLanguage.trim().toCharArray();
        String hanyupinyin = "";
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出拼音全部大写
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        // 不带声调
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        try {
            String str = String.valueOf(clChars[0]);
            if (str.matches(CHINESE)) {
                // 如果字符是中文,则将中文转为汉语拼音,并取第一个字母
                hanyupinyin = PinyinHelper.toHanyuPinyinStringArray(clChars[0], defaultFormat)[0].substring(0, 1);
                ;
            } else if (str.matches(NUMBER)) {
                // 如果字符是数字,取数字
                hanyupinyin += clChars[0];
            } else if (str.matches(LETTER)) {
                // 如果字符是字母,取字母
                hanyupinyin += clChars[0];
            } else {
                // 否则不转换
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
        }
        return hanyupinyin;
    }
}
