package com.yat.cache.anno.aop;

import com.yat.cache.anno.method.CacheHandler;
import com.yat.cache.anno.method.CacheInvokeConfig;
import com.yat.cache.anno.method.CacheInvokeContext;
import com.yat.cache.anno.support.ConfigMap;
import com.yat.cache.anno.support.ConfigProvider;
import com.yat.cache.anno.support.GlobalCacheConfig;
import com.yat.cache.core.JetCacheManager;
import lombok.Setter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

/**
 * 类JetCacheInterceptor的作用是作为方法拦截器，处理带有缓存注解的方法。
 * 它实现了MethodInterceptor接口，以便在执行特定方法之前后进行拦截操作，
 * 并依赖于Spring上下文中的相关Bean来配置和管理缓存。
 *
 * @author Yat
 */
public class JetCacheInterceptor implements MethodInterceptor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(JetCacheInterceptor.class);
    /**
     * 注入Spring上下文中的ConfigMap实例
     */
    @Setter
    @Autowired
    @SuppressWarnings("all")
    private ConfigMap cacheConfigMap;
    /**
     * Spring应用上下文，用于获取Bean实例
     */
    private ApplicationContext applicationContext;
    /**
     * 全局缓存配置
     */
    private GlobalCacheConfig globalCacheConfig;
    /**
     * 配置提供者，用于获取全局和方法级别的缓存配置
     */
    ConfigProvider configProvider;
    /**
     * 缓存管理器，用于实际的缓存操作
     */
    JetCacheManager jetCacheManager;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 拦截方法调用的入口。
     *
     * @param invocation 方法调用信息
     * @return 方法执行结果
     * @throws Throwable 如果方法执行过程中抛出异常
     */
    @Override
    public Object invoke(@NonNull final MethodInvocation invocation) throws Throwable {
        // 初始化配置提供者和全局缓存配置
        if (configProvider == null) {
            configProvider = applicationContext.getBean(ConfigProvider.class);
        }
        if (configProvider != null && globalCacheConfig == null) {
            globalCacheConfig = configProvider.getGlobalCacheConfig();
        }
        // 如果全局方法缓存未启用，则直接执行方法
        if (globalCacheConfig == null || !globalCacheConfig.isEnableMethodCache()) {
            return invocation.proceed();
        }
        // 初始化缓存管理器
        if (jetCacheManager == null) {
            jetCacheManager = applicationContext.getBean(JetCacheManager.class);
            if (jetCacheManager == null) {
                logger.error("There is no cache manager instance in spring context");
                return invocation.proceed();
            }
        }

        // 获取当前方法和目标对象
        Method method = invocation.getMethod();
        Object obj = invocation.getThis();
        // 获取方法的缓存配置
        CacheInvokeConfig cac = null;
        if (obj != null) {
            // 根据方法和对象类生成键，查找特定的缓存调用配置
            String key = CachePointcut.getKey(method, obj.getClass());
            cac = cacheConfigMap.getByMethodInfo(key);
        }

        /*
        if(logger.isTraceEnabled()){
            logger.trace("JetCacheInterceptor invoke. foundJetCacheConfig={}, method={}.{}(), targetClass={}",
                    cac != null,
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    invocation.getThis() == null ? null : invocation.getThis().getClass().getName());
        }
        */

        // 如果没有找到缓存配置，则直接执行方法
        if (cac == null || cac == CacheInvokeConfig.getNoCacheInvokeConfigInstance()) {
            return invocation.proceed();
        }

        // 创建缓存调用上下文并设置相关参数
        CacheInvokeContext context = configProvider.newContext(jetCacheManager).createCacheInvokeContext(cacheConfigMap);
        context.setTargetObject(invocation.getThis());
        context.setInvoker(invocation::proceed);
        context.setMethod(method);
        context.setArgs(invocation.getArguments());
        context.setCacheInvokeConfig(cac);
        context.setHiddenPackages(globalCacheConfig.getHiddenPackages());
        // 调用缓存处理器处理缓存逻辑
        return CacheHandler.invoke(context);
    }

}
