package com.taibai.common.core.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author angel
 * @Date 2020/4/8
 */
@Data
public class RabbitMqEntity<T> implements Serializable {

    /**
     * 数据
     */
    private T data;
}
