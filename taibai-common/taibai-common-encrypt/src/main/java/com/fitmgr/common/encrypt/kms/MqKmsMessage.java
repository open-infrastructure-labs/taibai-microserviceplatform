package com.fitmgr.common.encrypt.kms;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MqKmsMessage implements Serializable {

    private static final long serialVersionUID = 7196869757453551823L;

    /**
     * 接收服务名
     */
    private List<String> receiveName;

    /**
     * 发送服务名
     */
    private String sendName;

    /**
     * 批次号
     */
    private Integer batchNum;

    /**
     * 消息类型
     */
    private String msgType;
}
