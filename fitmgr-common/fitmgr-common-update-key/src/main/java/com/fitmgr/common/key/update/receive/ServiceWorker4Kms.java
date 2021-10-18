package com.fitmgr.common.key.update.receive;

import com.fitmgr.common.encrypt.kms.MqKmsMessage;

public interface ServiceWorker4Kms {

    /**
     * 更新数据秘钥, 各服务自己实现各自的业务
     * 
     * @param msg MQ消息
     * @return 返回更新结果
     */
    boolean updateDataKey(MqKmsMessage msg);

    /**
     * updateMasterKey
     * 
     * @param msg msg
     * @return boolean
     */
    boolean updateMasterKey(MqKmsMessage msg);

    /**
     * isLegalStatus
     * 
     * @param mqMsgType mqMsgType
     * @return boolean
     */
    boolean isLegalStatus(String mqMsgType);

    /**
     * necessaryWork4DataKey
     */
    void necessaryWork4DataKey();
}
