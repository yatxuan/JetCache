package com.yat.cache.anno.method;

import com.yat.cache.anno.support.CacheContext;
import com.yat.cache.anno.support.GlobalCacheConfig;
import com.yat.cache.anno.support.SpringConfigProvider;
import com.yat.cache.core.CacheManager;
import org.springframework.context.ApplicationContext;

/**
 * Created on 2016/10/19.
 *
 * @author huangli
 */
public class SpringCacheContext extends CacheContext {

    private ApplicationContext applicationContext;

    public SpringCacheContext(CacheManager cacheManager, SpringConfigProvider configProvider,
                              GlobalCacheConfig globalCacheConfig, ApplicationContext applicationContext) {
        super(cacheManager, configProvider, globalCacheConfig);
        this.applicationContext = applicationContext;
    }

    @Override
    protected CacheInvokeContext newCacheInvokeContext() {
        return new SpringCacheInvokeContext(applicationContext);
    }

}
