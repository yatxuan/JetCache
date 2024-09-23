package com.yat.cache.anno.support;

import com.yat.cache.core.Cache;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * ClassName CacheAnnoConfig
 * <p>Description 缓存配置</p>
 * 该类用于存储和管理缓存注解相关的配置信息，包括缓存区域、缓存名称、键值和条件等。
 *
 * @author Yat
 * Date 2024/8/20 11:26
 * version 1.0
 */
@Setter
@Getter
public class CacheAnnoConfig {

    /**
     * 如果在配置中配置了多个缓存区域，在这里指定使用哪个区域
     */
    private String area;
    /**
     * 指定缓存的唯一名称，不是必须的，
     * 如果没有指定，会使用类名+方法名。
     * name会被用于远程缓存的key前缀。
     * 另外在统计中，一个简短有意义的名字会提高可读性。
     */
    private String name;
    /**
     * 使用SpEL指定key，如果没有指定会根据所有参数自动生成。
     * SpEL是一种强大的表达式语言，可以在这里灵活的定义缓存键。
     */
    private String key;
    /**
     * 使用SpEL指定条件，如果表达式返回true的时候才去缓存中查询
     * 这允许在执行方法前基于某些条件来决定是否使用缓存。
     */
    private String condition;
    // =========== 以下是一些处理函数和属性 ====================
    /**
     * 用于评估条件表达式的函数
     */
    private Function<Object, Boolean> conditionEvaluator;
    /**
     * 用于评估缓存键表达式的函数
     */
    private Function<Object, Object> keyEvaluator;
    /**
     * 缓存实例
     */
    private Cache<?, ?> cache;
    /**
     * 定义该缓存配置的方法对象，用于内部调试或处理
     */
    private Method defineMethod;

}
