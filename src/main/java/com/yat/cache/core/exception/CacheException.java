package com.yat.cache.core.exception;

import java.io.Serial;

/**
 * ClassName CacheException
 * <p>Description 缓存异常类，用于处理缓存操作中出现的异常情况</p>
 *
 * @author Yat
 * Date 2024/8/22 20:20
 * version 1.0
 */
public class CacheException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -9066209768410752667L;

    /**
     * 使用指定的消息初始化异常对象
     *
     * @param message 异常消息，用于描述异常发生的原因
     */
    public CacheException(String message) {
        super(message);
    }

    /**
     * 使用指定的消息和原因初始化异常对象
     *
     * @param message 异常消息，用于描述异常发生的原因
     * @param cause   异常原因，通常为引起该异常的其他异常或错误
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因初始化异常对象
     *
     * @param cause 异常原因，通常为引起该异常的其他异常或错误
     */
    public CacheException(Throwable cause) {
        super(cause);
    }
}
