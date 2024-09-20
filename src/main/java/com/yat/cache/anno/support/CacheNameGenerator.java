/**
 * Created on 2018/3/22.
 */
package com.yat.cache.anno.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author huangli
 */
public interface CacheNameGenerator {

    String generateCacheName(Method method, Object targetObject);

    String generateCacheName(Field field);
}
