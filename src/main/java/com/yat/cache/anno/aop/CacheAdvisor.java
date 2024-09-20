package com.yat.cache.anno.aop;

import com.yat.cache.anno.support.ConfigMap;
import lombok.Setter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 缓存通知类，用于管理缓存的切面逻辑
 * 继承自AbstractBeanFactoryPointcutAdvisor，以实现Spring AOP的通知功能
 * 主要作用是根据配置动态设置切点和缓存配置
 *
 * @author Yat
 */
@Setter
public class CacheAdvisor extends AbstractBeanFactoryPointcutAdvisor {
    /**
     * 缓存通知Bean的名称，用于在Spring上下文中注册
     */
    public static final String CACHE_ADVISOR_BEAN_NAME = "jetCache2.internalCacheAdvisor";
    /**
     * 注入缓存配置Map，用于缓存的动态配置
     */
    @Autowired
    private ConfigMap cacheConfigMap;
    /**
     * 基础包名数组，指定需要扫描的包路径
     */
    private String[] basePackages;

    /**
     * 获取缓存切点（Pointcut）方法
     * 该方法返回一个CachePointcut实例，用于匹配特定的缓存操作方法
     *
     * @return 匹配缓存操作的切点对象
     */
    @Override
    public Pointcut getPointcut() {
        CachePointcut pointcut = new CachePointcut(basePackages);
        pointcut.setCacheConfigMap(cacheConfigMap);
        return pointcut;
    }

}
