package com.yat.cache.anno.method;

import com.yat.cache.anno.support.CacheAnnoConfig;
import com.yat.cache.core.JetCache;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * CacheInvokeContext 类用于封装缓存调用的上下文信息
 * 它包含了执行缓存操作时所需的所有方法、参数、配置以及目标对象等信息
 * 这些信息在缓存注解解析和缓存逻辑执行时起到关键作用
 *
 * @author Yat
 * Date 2024/8/22 21:54
 * version 1.0
 */
@Setter
@Getter
@NoArgsConstructor
public class CacheInvokeContext {
    /**
     * 方法调用者，用于实际执行方法
     */
    private Invoker invoker;

    /**
     * 将要执行的方法的Method对象
     */
    private Method method;
    /**
     * 方法的参数数组
     */
    private Object[] args;

    /**
     * 缓存调用配置，包含缓存操作的相关信息
     */
    private CacheInvokeConfig cacheInvokeConfig;
    /**
     * 目标对象，即拥有method方法的对象
     */
    private Object targetObject;

    /**
     * 方法执行的结果
     */
    private Object result;

    /**
     * 用于创建Cache实例的函数，根据当前的调用上下文和缓存注解配置来创建
     */
    private BiFunction<CacheInvokeContext, CacheAnnoConfig, JetCache> cacheFunction;
    /**
     * 隐藏的包名数组，这些包中的类不会被缓存机制监控
     */
    private String[] hiddenPackages;

}
