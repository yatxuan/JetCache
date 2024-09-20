/**
 * Created on  13-10-07 23:25
 */
package com.yat.cache.anno.method;

import org.springframework.context.ApplicationContext;

/**
 * @author huangli
 */
public class SpringCacheInvokeContext extends CacheInvokeContext {
    protected ApplicationContext context;

    public SpringCacheInvokeContext(ApplicationContext context) {
        this.context = context;
    }

    public Object bean(String name) {
        return context.getBean(name);
    }


}
