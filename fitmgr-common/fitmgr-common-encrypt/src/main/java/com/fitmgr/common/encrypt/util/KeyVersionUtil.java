package com.fitmgr.common.encrypt.util;

import com.fitmgr.common.encrypt.constant.EncryptConstant;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class KeyVersionUtil {

    /**
     * 秘钥版本自增方法
     * @param version 当前版本
     * @return 新版本
     */
   public String selfIncrease(String version) {
       return String.format("%0" + EncryptConstant.KEY_VERSION_LEN + "d", Integer.parseInt(version) + 1);
   }

}
