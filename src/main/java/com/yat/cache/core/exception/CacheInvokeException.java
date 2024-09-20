package com.yat.cache.core.exception;

import java.io.Serial;

/**
 * ClassName CacheInvokeException
 * <p>Description 缓存异常类，用于处理缓存调用中出现的异常情况</p>
 *
 * @author Yat
 * Date 2024/8/22 20:22
 * version 1.0
 */
public class CacheInvokeException extends CacheException {

    @Serial
    private static final long serialVersionUID = -9002505061387176702L;

    public CacheInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheInvokeException(Throwable cause) {
        super(cause);
    }

}
