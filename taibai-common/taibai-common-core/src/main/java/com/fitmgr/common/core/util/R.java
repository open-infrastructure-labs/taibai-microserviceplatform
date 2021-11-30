
package com.taibai.common.core.util;

import java.io.Serializable;

import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.constant.enums.ResponseCodeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 响应信息主体
 *
 * @param <T>
 * @author Taibai
 */
@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int code = CommonConstants.SUCCESS;

    @Getter
    @Setter
    private String msg = "success";

    @Getter
    @Setter
    private T data;

    public R() {
        super();
    }

    public R(T data) {
        super();
        this.data = data;
    }

    public R(T data, String msg) {
        super();
        this.data = data;
        this.msg = msg;
    }

    public R(Throwable e) {
        super();
        this.msg = e.getMessage();
        this.code = CommonConstants.FAIL;
    }

    public static <T> R<T> ok() {
        return restResult(null, ResponseCodeEnum.SUCCESS.getCode(), ResponseCodeEnum.SUCCESS.getDesc());
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, ResponseCodeEnum.SUCCESS.getCode(), ResponseCodeEnum.SUCCESS.getDesc());
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, ResponseCodeEnum.SUCCESS.getCode(), msg);
    }

    public static <T> R<T> ok(T data, int code) {
        return restResult(data, code, ResponseCodeEnum.SUCCESS.getDesc());
    }

    public static <T> R<T> failed() {
        return restResult(null, ResponseCodeEnum.ERROR.getCode(), ResponseCodeEnum.ERROR.getDesc());
    }

    public static <T> R<T> failed(String msg) {
        return restResult(null, ResponseCodeEnum.ERROR.getCode(), msg);
    }

    public static <T> R<T> failed(T data) {
        return restResult(data, ResponseCodeEnum.ERROR.getCode(), ResponseCodeEnum.ERROR.getDesc());
    }

    public static <T> R<T> failed(T data, String msg) {
        return restResult(data, ResponseCodeEnum.ERROR.getCode(), msg);
    }

    public static <T> R<T> failed(T data, BusinessEnum businessEnum) {
        return restResult(data, businessEnum.getCode(), businessEnum.getDescription());
    }

    public static <T> R<T> failed(BusinessEnum businessEnum) {
        return restResult(null, businessEnum.getCode(), businessEnum.getDescription());
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
