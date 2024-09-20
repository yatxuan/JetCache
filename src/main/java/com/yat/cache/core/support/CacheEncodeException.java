package com.yat.cache.core.support;

import com.yat.cache.core.CacheException;

/**
 * Created on 2016/12/23.
 *
 * @author huangli
 */
public class CacheEncodeException extends CacheException {

    private static final long serialVersionUID = -1768444197009616269L;

    public CacheEncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheEncodeException(String message) {
        super(message);
    }

}
