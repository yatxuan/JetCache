package com.yat.cache.anno.config;

import com.yat.cache.anno.aop.CacheAdvisor;
import com.yat.cache.anno.aop.JetCacheInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * JetCache代理配置类
 * 该类用于配置JetCache的AOP代理，通过设置拦截器和顾问来实现方法级别的缓存功能
 *
 * @author Yat
 * Date 2024/8/22 21:17
 * version 1.0
 */
@Configuration
public class JetCacheProxyConfiguration implements ImportAware, ApplicationContextAware {

    /**
     * Spring应用上下文
     */
    private ApplicationContext applicationContext;
    /**
     * 用于存储@EnableMethodCache注解的属性
     */
    protected AnnotationAttributes enableMethodCache;

    /**
     * 设置导入元数据
     * 从导入元数据中提取@EnableMethodCache注解的属性
     *
     * @param importMetadata 导入元数据
     * @throws IllegalArgumentException 如果@EnableMethodCache注解不在导入类上则抛出异常
     */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableMethodCache = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableMethodCache.class.getName(), false));
        if (this.enableMethodCache == null) {
            throw new IllegalArgumentException(
                    "@EnableMethodCache is not present on importing class " + importMetadata.getClassName()
            );
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建并配置CacheAdvisor bean
     * 该Advisor负责将JetCacheInterceptor应用到指定的包下的所有方法
     *
     * @param jetCacheInterceptor JetCache拦截器
     * @return 配置好的CacheAdvisor实例
     */
    @Bean(name = CacheAdvisor.CACHE_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheAdvisor jetcacheAdvisor(JetCacheInterceptor jetCacheInterceptor) {
        CacheAdvisor advisor = new CacheAdvisor();
        advisor.setAdvice(jetCacheInterceptor);
        advisor.setBasePackages(this.enableMethodCache.getStringArray("basePackages"));
        advisor.setOrder(this.enableMethodCache.<Integer>getNumber("order"));
        return advisor;
    }

    /**
     * 创建JetCacheInterceptor bean
     * 该拦截器负责实际的缓存逻辑
     *
     * @return 新的JetCacheInterceptor实例
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public JetCacheInterceptor jetCacheInterceptor() {
        return new JetCacheInterceptor();
    }

}
