package com.yat.cache.anno.method;

import com.yat.cache.anno.support.CacheContext;
import com.yat.cache.anno.support.GlobalCacheConfig;
import com.yat.cache.anno.support.SpringConfigProvider;
import com.yat.cache.core.JetCacheManager;
import org.springframework.context.ApplicationContext;

/**
 * ClassName SpringCacheContext
 * <p>
 * SpringCache上下文类，扩展自CacheContext
 * 该类提供了在Spring环境下管理缓存调用上下文的功能
 * 它持有Spring的ApplicationContext以便能够利用Spring的依赖注入特性
 *
 * @author Yat
 * Date 2024/8/22 22:01
 * version 1.0
 */
public class SpringCacheContext extends CacheContext {

    private final ApplicationContext applicationContext;

    /**
     * 初始化SpringCacheContext
     *
     * @param jetCacheManager    缓存管理器，负责缓存的创建、维护和销毁
     * @param configProvider     配置提供者，用于从配置属性文件中读取缓存配置
     * @param globalCacheConfig  全局缓存配置，用于存储全局缓存设置
     * @param applicationContext Spring应用上下文，提供Spring环境下的依赖注入功能
     */
    public SpringCacheContext(
            JetCacheManager jetCacheManager, SpringConfigProvider configProvider,
            GlobalCacheConfig globalCacheConfig, ApplicationContext applicationContext
    ) {
        super(jetCacheManager, configProvider, globalCacheConfig);
        this.applicationContext = applicationContext;
    }

    @Override
    protected CacheInvokeContext newCacheInvokeContext() {
        return new SpringCacheInvokeContext(applicationContext);
    }

}
