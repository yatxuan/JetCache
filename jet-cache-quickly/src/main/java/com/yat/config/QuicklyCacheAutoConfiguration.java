package com.yat.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.yat.cache.anno.api.CacheType;
import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.autoconfigure.AutoConfigureBeans;
import com.yat.cache.autoconfigure.properties.JetCacheProperties;
import com.yat.cache.autoconfigure.properties.LocalCacheProperties;
import com.yat.cache.autoconfigure.properties.RemoteCacheProperties;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.JetCache;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.embedded.CaffeineCacheBuilder;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.support.convertor.GsonKeyConvertor;
import com.yat.cache.core.support.encoders.Kryo5ValueDecoder;
import com.yat.cache.core.support.encoders.Kryo5ValueEncoder;
import com.yat.cache.core.template.QuickConfig;
import com.yat.cache.redis.springdata.RedisSpringDataCacheBuilder;
import com.yat.utils.JetCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName QuicklyCacheAutoConfiguration
 * Description 缓存快速装配
 *
 * @author Yat
 * Date 2024/9/25 16:36
 * version 1.0
 */
@Configuration
public class QuicklyCacheAutoConfiguration {

    private AutoConfigureBeans autoConfigureBeans;
    private ApplicationContext applicationContext;
    private JetCacheProperties jetCacheProperties;

    @Bean(DefaultCacheConstant.BEAN_KEY_CUSTOM)
    public KeyConvertor keyConvertor() {
        return GsonKeyConvertor.INSTANCE;
    }

    @Bean
    @SuppressWarnings("all")
    public JetCache<Object, Object> jetCache(@Autowired JetCacheManager cacheManager) {
        init();

        QuickConfig quickConfig = getQuickConfig(autoConfigureBeans, jetCacheProperties);
        return cacheManager.getOrCreateCache(quickConfig);
    }

    private QuickConfig getQuickConfig(AutoConfigureBeans beans, JetCacheProperties properties) {

        Map<String, CacheBuilder> remoteCacheBuilders = beans.getRemoteCacheBuilders();
        Map<String, CacheBuilder> localCacheBuilders = beans.getLocalCacheBuilders();

        CacheType cacheType = CacheType.LOCAL;
        if (!localCacheBuilders.isEmpty() && !remoteCacheBuilders.isEmpty()) {
            cacheType = CacheType.BOTH;
        } else if (!remoteCacheBuilders.isEmpty()) {
            cacheType = CacheType.REMOTE;
        }

        QuickConfig.Builder builder = QuickConfig.newBuilder(JetCacheUtil.cacheName)
                .cacheType(cacheType)
                .expire(Duration.ofSeconds(100));

        if (Objects.nonNull(properties)) {
            builder.penetrationProtect(properties.isPenetrationProtect());
        }

        return builder.build();
    }

    /**
     * 自动配置 默认配置
     */
    private void init() {

        String area = DefaultCacheConstant.DEFAULT_AREA;

        Map<String, CacheBuilder> remoteCacheBuilders = autoConfigureBeans.getRemoteCacheBuilders();
        Map<String, CacheBuilder> localCacheBuilders = autoConfigureBeans.getLocalCacheBuilders();

        // 获取缓存配置
        RemoteCacheProperties remoteCacheProperties = null;
        LocalCacheProperties localCacheProperties = null;
        if (Objects.nonNull(jetCacheProperties)) {
            // 判断用户是否配置了缓存
            Map<String, RemoteCacheProperties> remoteCache = jetCacheProperties.getRemoteCache();
            if (CollUtil.isNotEmpty(remoteCache)) {
                remoteCacheProperties = remoteCache.get(area);
            }
            Map<String, LocalCacheProperties> localCache = jetCacheProperties.getLocalCache();
            if (CollUtil.isNotEmpty(localCache)) {
                localCacheProperties = localCache.get(area);
            }
        }

        if (!remoteCacheBuilders.containsKey(area)) {
            if (Objects.isNull(remoteCacheProperties)) {
                remoteCacheProperties = new RemoteCacheProperties();
                remoteCacheProperties.setKeyConvertor(null);
            }
            RedisConnectionFactory factory = getConnectionFactory(applicationContext, jetCacheProperties);
            if (Objects.nonNull(factory)) {
                RedisSpringDataCacheBuilder.RedisSpringDataCacheBuilderImpl redisSpringDataCacheBuilder =
                        RedisSpringDataCacheBuilder.createBuilder().connectionFactory(factory);
                if (Objects.nonNull(jetCacheProperties)) {
                    Map<String, RemoteCacheProperties> remoteCache = jetCacheProperties.getRemoteCache();
                    if (CollUtil.isNotEmpty(remoteCache)) {
                        remoteCacheProperties = remoteCache.get(area);
                    }
                }
                // 配置远程缓存
                parseRemoteGeneralConfig(
                        redisSpringDataCacheBuilder, remoteCacheProperties
                );
                // 放入缓存管理器
                remoteCacheBuilders.put(area, redisSpringDataCacheBuilder);
            }
        }

        if (!localCacheBuilders.containsKey(area)) {
            if (Objects.isNull(localCacheProperties)) {
                localCacheProperties = new LocalCacheProperties();
                localCacheProperties.setKeyConvertor(null);
            }

            CaffeineCacheBuilder.CaffeineCacheBuilderImpl builder = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                    .keyConvertor(keyConvertor())
                    .limit(DefaultCacheConstant.DEFAULT_LOCAL_LIMIT);

            localCacheBuilders.put(area, builder);
        }
    }

    /**
     * Description: 获取连接工厂
     * <p>
     * Date: 2024/9/26 15:03
     *
     * @param applicationContext 上下文
     * @param jetCacheProperties 配置
     * @return {@link RedisConnectionFactory}
     */
    @SuppressWarnings("unused")
    private RedisConnectionFactory getConnectionFactory(
            ApplicationContext applicationContext, JetCacheProperties jetCacheProperties
    ) {
        Map<String, RedisConnectionFactory> beans = applicationContext.getBeansOfType(RedisConnectionFactory.class);
        if (CollUtil.isEmpty(beans)) {
            return null;
        }
        RedisConnectionFactory factory = beans.values().iterator().next();
        if (beans.size() > 1) {
            // 连接工厂 大于1 不进行自动选择 根据配置进行选择,如果未配置, 则不创建此缓存
            if (Objects.isNull(jetCacheProperties) || CollUtil.isEmpty(jetCacheProperties.getRemoteCache())) {
                return null;
            }
            RemoteCacheProperties properties = jetCacheProperties.getRemoteCache()
                    .get(DefaultCacheConstant.DEFAULT_AREA);
            if (Objects.isNull(properties) || Objects.isNull(properties.getRedisData())) {
                return null;
            }
            String connectionFactoryName = properties.getRedisData().getConnectionFactory();
            if (StrUtil.isBlank(connectionFactoryName) || !beans.containsKey(connectionFactoryName)) {
                return null;
            }
            return beans.get(connectionFactoryName);
        }
        try (RedisConnection connection = factory.getConnection()) {
            return factory;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Description: 解析远程缓存配置
     * <p>
     * Date: 2024/9/26 15:02
     *
     * @param builder    缓存构建器
     * @param properties 缓存配置
     */
    private void parseRemoteGeneralConfig(ExternalCacheBuilder<?> builder, RemoteCacheProperties properties) {

        builder.keyConvertor(keyConvertor());

        // 设置缓存键的前缀，使用键转换器的名称
        builder.setKeyPrefix(properties.getKeyPrefix());
        // 解析并设置广播通道
        if (StrUtil.isNotBlank(properties.getBroadcastChannel())) {
            builder.setBroadcastChannel(properties.getBroadcastChannel().trim());
        }

        // 创建一个解析函数，用于根据序列化策略解析数据
        builder.setValueEncoder(new Kryo5ValueEncoder(true));
        builder.setValueDecoder(new Kryo5ValueDecoder(true));
    }

    @Autowired
    @SuppressWarnings("all")
    public void setAutoConfigureBeans(AutoConfigureBeans autoConfigureBeans) {
        this.autoConfigureBeans = autoConfigureBeans;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired(required = false)
    public void setJetCacheProperties(JetCacheProperties jetCacheProperties) {
        this.jetCacheProperties = jetCacheProperties;
    }
}
