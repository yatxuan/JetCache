package com.yat.cache.core.exception;

import java.io.Serial;

/**
 * ClassName CacheEncodeException
 * <p>Description 缓存编码异常类，用于表示在缓存编码过程中发生的异常。</p>
 *
 * @author Yat
 * Date 2024/8/22 17:45
 * version 1.0
 */
public class CacheEncodeException extends CacheException {

    @Serial
    private static final long serialVersionUID = -1768444197009616269L;

    /**
     * 创建一个带有消息和原因的缓存编码异常。
     *
     * @param message 异常消息
     * @param cause   导致此异常的原因
     */
    public CacheEncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个带有消息的缓存编码异常。
     *
     * @param message 异常消息
     */
    public CacheEncodeException(String message) {
        super(message);
    }

}
