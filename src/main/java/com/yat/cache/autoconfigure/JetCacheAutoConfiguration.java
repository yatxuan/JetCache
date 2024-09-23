package com.yat.cache.autoconfigure;

import com.yat.cache.anno.support.EncoderParser;
import com.yat.cache.anno.support.GlobalCacheConfig;
import com.yat.cache.anno.support.JetCacheBaseBeans;
import com.yat.cache.anno.support.KeyConvertorParser;
import com.yat.cache.anno.support.SpringConfigProvider;
import com.yat.cache.autoconfigure.constants.BeanNameConstant;
import com.yat.cache.core.SimpleCacheManager;
import com.yat.cache.core.support.StatInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.function.Consumer;

/**
 * ClassName JetCacheAutoConfiguration
 * <p>Description JetCacheAutoConfiguration</p>
 *
 * @author Yat
 * Date 2024/9/23 10:38
 * version 1.0
 */
@Configuration
@ConditionalOnClass(GlobalCacheConfig.class)
@ConditionalOnMissingBean(GlobalCacheConfig.class)
@EnableConfigurationProperties(JetCacheProperties.class)
@Import({
        CaffeineAutoConfiguration.class,
        MockRemoteCacheAutoConfiguration.class,
        LinkedHashMapAutoConfiguration.class,
        RedisLettuceAutoConfiguration.class,
        RedisSpringDataAutoConfiguration.class
})
public class JetCacheAutoConfiguration {


    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public SpringConfigProvider springConfigProvider(
            @Autowired ApplicationContext applicationContext,
            @Autowired GlobalCacheConfig globalCacheConfig,
            @Autowired(required = false) EncoderParser encoderParser,
            @Autowired(required = false) KeyConvertorParser keyConvertorParser,
            @Autowired(required = false) Consumer<StatInfo> metricsCallback) {
        return new JetCacheBaseBeans().springConfigProvider(applicationContext, globalCacheConfig,
                encoderParser, keyConvertorParser, metricsCallback);
    }

    @Bean(name = BeanNameConstant.JC_CACHE_MANAGER_NAME, destroyMethod = "close")
    @ConditionalOnMissingBean
    public SimpleCacheManager cacheManager(@Autowired SpringConfigProvider springConfigProvider) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCacheBuilderTemplate(springConfigProvider.getCacheBuilderTemplate());
        return cacheManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoConfigureBeans autoConfigureBeans() {
        return new AutoConfigureBeans();
    }

    @Bean(name = BeanNameConstant.GLOBAL_CACHE_CONFIG_NAME)
    public GlobalCacheConfig globalCacheConfig(AutoConfigureBeans autoConfigureBeans, JetCacheProperties props) {
        GlobalCacheConfig _globalCacheConfig = new GlobalCacheConfig();
        _globalCacheConfig.setHiddenPackages(props.getHiddenPackages());
        _globalCacheConfig.setStatIntervalMinutes(props.getStatIntervalMinutes());
        _globalCacheConfig.setAreaInCacheName(props.getAreaInCacheName());
        _globalCacheConfig.setPenetrationProtect(props.isPenetrationProtect());
        _globalCacheConfig.setEnableMethodCache(props.isEnableMethodCache());
        _globalCacheConfig.setLocalCacheBuilders(autoConfigureBeans.getLocalCacheBuilders());
        _globalCacheConfig.setRemoteCacheBuilders(autoConfigureBeans.getRemoteCacheBuilders());
        return _globalCacheConfig;
    }

    @Bean
    public static BeanDependencyManager beanDependencyManager() {
        return new BeanDependencyManager();
    }

}
