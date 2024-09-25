package com.yat.cache.core;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ResultData类用于封装缓存操作的结果。
 * 包含了操作结果码、提示信息以及操作返回的数据。
 *
 * @author Yat
 * Date 2024/8/22 20:39
 * version 1.0
 */
@Data
@AllArgsConstructor
public class ResultData {

    /**
     * 缓存操作结果码。
     */
    private CacheResultCode resultCode;

    /**
     * 操作提示信息。
     */
    private String message;

    /**
     * 操作返回的数据。
     */
    private Object data;

    /**
     * 用于处理异常情况下的缓存操作结果。
     *
     * @param e 异常对象，表示缓存操作失败的原因。
     */
    public ResultData(Throwable e) {
        // 设置操作结果码为失败
        this.resultCode = CacheResultCode.FAIL;
        // 记录异常类名和异常信息作为提示信息
        this.message = "Ex : " + e.getClass() + ", " + e.getMessage();
    }

    /**
     * 获取操作返回的数据。
     * 此方法用于需要解包数据的场景。
     *
     * @return 解包后的数据。
     */
    public Object getData() {
        // 调用CacheGetResult的unwrapValue方法解包
        return CacheGetResult.unwrapValue(data);
    }

    /**
     * 获取原始的操作返回数据。
     *
     * @return 原始数据。
     */
    public Object getOriginData() {
        return data;
    }

}
