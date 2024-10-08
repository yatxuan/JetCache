package com.yat.cache.anno.method;

import org.springframework.context.ApplicationContext;

/**
 * ClassName SpringCacheInvokeContext
 * SpringCacheInvokeContext 类继承自 CacheInvokeContext，用于在 Spring 上下文中执行缓存操作。
 * 它利用 Spring 的依赖注入功能来获取Bean实例，从而执行缓存相关的操作。
 *
 * @author Yat
 * Date 2024/8/22 22:01
 * version 1.0
 */
public class SpringCacheInvokeContext extends CacheInvokeContext {

    protected final ApplicationContext context;

    /**
     * 初始化 SpringCacheInvokeContext 实例。
     *
     * @param context Spring应用上下文，用于依赖注入
     */
    public SpringCacheInvokeContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * 根据名称从Spring上下文中获取Bean实例。
     *
     * @param name Bean的名称
     * @return 对应名称的Bean实例
     */
    public Object bean(String name) {
        return context.getBean(name);
    }


}
