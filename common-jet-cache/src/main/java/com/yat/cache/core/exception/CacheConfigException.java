package com.yat.cache.core.exception;

import java.io.Serial;

/**
 * ClassName CacheConfigException
 * <p>Description CacheConfigException</p>
 *
 * @author Yat
 * Date 2024/8/21 下午11:16
 * version 1.0
 */
public class CacheConfigException extends CacheException {

    @Serial
    private static final long serialVersionUID = -3401839239922905427L;

    public CacheConfigException(Throwable cause) {
        super(cause);
    }

    public CacheConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheConfigException(String message) {
        super(message);
    }
}
