package com.yat.cache.anno.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * ClassName JetCacheRefresh
 * <p>Description 用于配置缓存刷新策略: 需求配合 {@link Cached} 一起使用</p>
 *
 * <p>
 *     todo 重命名 JetCacheRefresh
 * </p>
 * @author Yat
 * Date 2024/8/22 09:52
 * version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CacheRefresh {

    /**
     * 指定缓存刷新间隔时间（秒）。
     *
     * @return 缓存刷新间隔时间
     */
    int refresh();

    /**
     * 指定在最后一次访问后停止刷新的时间（秒）。
     * 如果未指定，则使用全局配置，
     * 如果也没有定义全局配置，则使用无限期缓存。
     *
     * @return 停止刷新的时间
     */
    int stopRefreshAfterLastAccess() default CacheConsts.UNDEFINED_INT;

    /**
     * 指定刷新锁的超时时间（秒）。
     * 如果未指定，则使用全局配置，
     * 如果也没有定义全局配置，则使用无限期缓存。
     *
     * @return 刷新锁的超时时间
     */
    int refreshLockTimeout() default CacheConsts.UNDEFINED_INT;

    /**
     * 指定时间单位。（默认:秒）
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
