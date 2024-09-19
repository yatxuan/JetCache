package com.yat.cache.anno.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName JetCacheInvalidateContainer
 * <p>Description 用于收集多个 {@link CacheInvalidate} 注解</p>
 *
 * <p>
 *  todo 重命名 JetCacheRemoveContainer
 * </p>
 *
 * @author Yat
 * <p>
 * Date 2024/8/22 09:50
 * version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheInvalidateContainer {

    /**
     * 返回 {@link CacheInvalidate} 注解数组。
     */
    CacheInvalidate[] value();
}
