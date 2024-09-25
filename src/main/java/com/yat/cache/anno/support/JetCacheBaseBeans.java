package com.yat.cache.anno.support;

import com.yat.cache.autoconfigure.constants.BeanNameConstant;
import com.yat.cache.core.SimpleCacheManager;
import com.yat.cache.core.support.StatInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

/**
 * ClassName JetCacheBaseBeans
 * <p>Description 用于非 spring-boot 项目的 JetCache 基础 Bean 定义</p>
 * <p>在非 Spring Boot 环境下，需要通过定义 Bean 来初始化和管理缓存</p>
 *
 * @author Yat
 * Date 2024/8/22 09:17
 * version 1.0
 */
public class JetCacheBaseBeans {

    /**
     * 配置并初始化 Spring 配置提供者
     * <p>这里设置了多个依赖项，包括应用程序上下文、全局缓存配置和其他可选组件</p>
     *
     * @param applicationContext 应用程序上下文，用于获取 Spring 管理的 bean
     * @param globalCacheConfig  全局缓存配置
     * @param encoderParser      编码器解析器，可选
     * @param keyConvertorParser 键转换器解析器，可选
     * @param metricsCallback    监听回调，用于收集统计信息，可选
     * @return SpringConfigProvider 配置初始化后的 Spring 配置提供者实例
     */
    @Bean(destroyMethod = "shutdown")
    public SpringConfigProvider springConfigProvider(
            @Autowired ApplicationContext applicationContext,
            @Autowired GlobalCacheConfig globalCacheConfig,
            @Autowired(required = false) EncoderParser encoderParser,
            @Autowired(required = false) KeyConvertorParser keyConvertorParser,
            @Autowired(required = false) Consumer<StatInfo> metricsCallback) {
        SpringConfigProvider cp = createConfigProvider();
        cp.setApplicationContext(applicationContext);
        cp.setGlobalCacheConfig(globalCacheConfig);

        if (encoderParser != null) {
            cp.setEncoderParser(encoderParser);
        }

        if (keyConvertorParser != null) {
            cp.setKeyConvertorParser(keyConvertorParser);
        }

        if (metricsCallback != null) {
            cp.setMetricsCallback(metricsCallback);
        }
        cp.init();
        return cp;
    }

    /**
     * 创建 Spring 配置提供者实例
     * <p>用于配置缓存的一些全局设置</p>
     *
     * @return SpringConfigProvider 配置提供者实例
     */
    protected SpringConfigProvider createConfigProvider() {
        return new SpringConfigProvider();
    }

    /**
     * 创建并配置缓存管理器
     * <p>缓存管理器负责管理各种缓存实例</p>
     *
     * @param configProvider 缓存配置提供者，提供缓存配置信息
     * @return SimpleCacheManager 配置后的缓存管理器实例
     */
    @Bean(name = BeanNameConstant.JC_CACHE_MANAGER_NAME, destroyMethod = "close")
    public SimpleCacheManager cacheManager(@Autowired ConfigProvider configProvider) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCacheBuilderTemplate(configProvider.getCacheBuilderTemplate());
        return cacheManager;
    }
}
