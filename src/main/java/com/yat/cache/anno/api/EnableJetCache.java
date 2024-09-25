package com.yat.cache.anno.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName EnableJetCache
 * <p>Description 用于启用缓存功能</p>
 *
 * @author Yat
 * Date 2024/8/22 09:55
 * version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnableJetCache {
}
