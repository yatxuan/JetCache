package com.yat.cache.anno.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * ClassName JetCachePenetrationProtect
 * <p>Description 用于防止缓存穿透攻击: 需求配合 {@link Cached} 一起使用</p>
 *
 * <p>
 *     todo 重命名 JetCachePenetrationProtect
 * </p>
 * @author Yat
 * Date 2024/8/22 09:51
 * version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CachePenetrationProtect {

    /**
     * 指定是否启用防止缓存穿透保护。
     *
     * @return 是否启用防止缓存穿透保护
     */
    boolean value() default true;

    /**
     * 指定缓存穿透保护的有效时间（毫秒）。
     * 使用全局配置如果未指定此属性值，
     * 如果也未定义全局配置，则使用无限期缓存。
     *
     * @return 缓存穿透保护的有效时间
     */
    int timeout() default CacheConsts.UNDEFINED_INT;

    /**
     * 指定有效时间的时间单位。默认为秒。
     *
     * @return 有效时间的时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
