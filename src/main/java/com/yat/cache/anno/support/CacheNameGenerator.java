package com.yat.cache.anno.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 类名：CacheNameGenerator
 * <p>描述：用于生成缓存名称的接口</p>
 *
 * @author Yat
 * Date: 2024/8/22 22:02
 * 版本：1.0
 */
public interface CacheNameGenerator {

    /**
     * 根据方法和目标对象生成缓存名称
     *
     * @param method       调用的方法
     * @param targetObject 目标对象
     * @return 生成的缓存名称
     */
    String generateCacheName(Method method, Object targetObject);

    /**
     * 根据字段生成缓存名称
     *
     * @param field 相关字段
     * @return 生成的缓存名称
     */
    String generateCacheName(Field field);
}
