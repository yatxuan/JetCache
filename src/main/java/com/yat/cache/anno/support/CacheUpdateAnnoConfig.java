package com.yat.cache.anno.support;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

/**
 * CacheUpdateAnnoConfig类用于配置缓存更新注解的相关属性
 * 它继承自CacheAnnoConfig类，并添加了用于缓存更新操作的具体配置项
 *
 * @author Yat
 * Date 2024/8/22 22:03
 * version 1.0
 */
@Setter
@Getter
public class CacheUpdateAnnoConfig extends CacheAnnoConfig {

    /**
     * 缓存键的名称
     */
    private String value;
    /**
     * 标记缓存更新操作是否支持多数据更新
     */
    private boolean multi;
    /**
     * 用于动态计算缓存键的函数
     * 在某些情况下，缓存键可能需要根据运行时数据动态计算
     */
    private Function<Object, Object> valueEvaluator;

}
