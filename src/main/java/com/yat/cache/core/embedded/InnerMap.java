package com.yat.cache.core.embedded;

import java.util.Collection;
import java.util.Map;

/**
 * ClassName InnerMap
 * <p>Description InnerMap</p>
 *
 * @author Yat
 * Date 2024/8/22 11:31
 * version 1.0
 */
public interface InnerMap {
    /**
     * Description: 获取指定键对应的值
     * <p>
     * Date: 2024/8/22 11:32
     *
     * @param key 键
     * @return {@link Object}
     */
    Object getValue(Object key);

    /**
     * Description: 获取多个键对应的值集合
     * <p>
     * Date: 2024/8/22 11:32
     *
     * @param keys 键集合
     * @return {@link Map}
     */
    Map getAllValues(Collection keys);

    /**
     * Description: 将指定的键值对放入映射中
     * <p>
     * Date: 2024/8/22 11:33
     *
     * @param key   键
     * @param value 值
     */
    void putValue(Object key, Object value);

    /**
     * Description: 将指定的键值对集合放入映射中
     * <p>
     * Date: 2024/8/22 11:34
     *
     * @param map 键值对集合
     */
    void putAllValues(Map map);

    /**
     * Description: 移除指定键对应的条目
     * <p>
     * Date: 2024/8/22 11:34
     *
     * @param key 键
     * @return {@link boolean}
     */
    boolean removeValue(Object key);

    /**
     * Description: 如果映射中不存在指定的键，则放入指定的键值对
     * <p>
     * Date: 2024/8/22 11:34
     *
     * @param key   键
     * @param value 值
     * @return {@link boolean}
     */
    boolean putIfAbsentValue(Object key, Object value);

    /**
     * Description: 移除多个键对应的条目
     * <p>
     * Date: 2024/8/22 11:34
     *
     * @param keys 键集合
     */
    void removeAllValues(Collection keys);
}
