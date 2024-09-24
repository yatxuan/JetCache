package com.yat.cache.autoconfigure.init;

import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.anno.support.ParserFunction;
import com.yat.cache.autoconfigure.AutoConfigureBeans;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
import com.yat.cache.autoconfigure.properties.JetCacheProperties;
import com.yat.cache.autoconfigure.properties.LocalCacheProperties;
import com.yat.cache.autoconfigure.properties.RemoteCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.BaseCacheType;
import com.yat.cache.autoconfigure.properties.enums.KeyConvertorEnum;
import com.yat.cache.core.AbstractCacheBuilder;
import com.yat.cache.core.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName AbstractCacheAutoInit
 * <p>Description 缓存自动装配</p>
 * 负责在Spring上下文初始化时，根据配置自动创建和注册缓存实例
 *
 * @author Yat
 * Date 2024/8/22 22:08
 * version 1.0
 */
public abstract class AbstractCacheAutoInit implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCacheAutoInit.class);
    /**
     * 本地缓存的前缀
     */
    private static final String LOCAL_PREFIX = "local.";
    /**
     * 远程缓存的前缀
     */
    private static final String REMOTE_PREFIX = "remote.";

    private final ReentrantLock reentrantLock = new ReentrantLock();
    /**
     * 标记是否已经初始化
     */
    private volatile boolean inited = false;
    /**
     * 缓存配置
     */
    protected JetCacheProperties jetCacheProperties;
    /**
     * 自动配置的Bean，用于获取缓存构建器
     */
    protected AutoConfigureBeans autoConfigureBeans;
    /**
     * 支持的缓存类型列表
     * 用于过滤哪些类型的缓存应该被初始化
     */
    protected List<String> typeNames;

    /**
     * 初始化支持的缓存类型
     *
     * @param cacheTypes 不能为null或空的缓存类型列表
     */
    public AbstractCacheAutoInit(String... cacheTypes) {
        Objects.requireNonNull(cacheTypes, "cacheTypes can't be null");
        Assert.isTrue(cacheTypes.length > 0, "cacheTypes length is 0");
        this.typeNames = Arrays.stream(cacheTypes).toList();
    }

    @Override
    public void afterPropertiesSet() {
        if (!inited) {
            reentrantLock.lock();
            try {
                if (!inited) {
                    // 初始化本地缓存和远程缓存
                    localProcess(autoConfigureBeans.getLocalCacheBuilders());
                    remoteProcess(autoConfigureBeans.getRemoteCacheBuilders());
                    inited = true;
                }
            } finally {
                reentrantLock.unlock();
            }
        }
    }

    /**
     * 处理本地缓存的初始化
     *
     * @param cacheBuilders 缓存构建器的映射，用于存储初始化后的缓存实例
     */
    private void localProcess(Map<String, CacheBuilder> cacheBuilders) {
        Set<Map.Entry<String, LocalCacheProperties>> entries = jetCacheProperties.getLocalCache().entrySet();
        for (Map.Entry<String, LocalCacheProperties> entry : entries) {
            LocalCacheProperties localCacheProperties = entry.getValue();
            String cacheArea = entry.getKey();
            BaseCacheType type = localCacheProperties.getType();
            if (!typeNames.contains(type.getUpperName())) {
                continue;
            }
            logger.info("init cache area {} , LocalType= {}", cacheArea, typeNames.get(0));
            CacheBuilder cacheBuilder = initCache(localCacheProperties, LOCAL_PREFIX + cacheArea);
            cacheBuilders.put(cacheArea, cacheBuilder);
        }
    }

    /**
     * 处理远程缓存的初始化
     *
     * @param cacheBuilders 缓存构建器的映射，用于存储初始化后的缓存实例
     */
    private void remoteProcess(Map<String, CacheBuilder> cacheBuilders) {
        Set<Map.Entry<String, RemoteCacheProperties>> entries = jetCacheProperties.getRemoteCache().entrySet();
        for (Map.Entry<String, RemoteCacheProperties> entry : entries) {
            RemoteCacheProperties remoteCacheProperties = entry.getValue();
            String cacheArea = entry.getKey();
            BaseCacheType type = remoteCacheProperties.getType();
            if (!typeNames.contains(type.getUpperName())) {
                continue;
            }
            logger.info("init cache area {} , RemoteType= {}", cacheArea, typeNames.get(0));

            CacheBuilder cacheBuilder = initCache(remoteCacheProperties, REMOTE_PREFIX + cacheArea);
            cacheBuilders.put(cacheArea, cacheBuilder);
        }
    }

    /**
     * 初始化缓存的抽象方法
     *
     * @param cacheProperties     缓存属性，用于配置缓存的行为和特性
     * @param cacheAreaWithPrefix 带前缀的缓存区域，用于区分和组织不同的缓存空间
     * @return 返回一个初始化后的缓存构建器，用于进一步设置或操作缓存
     * <p>
     * 此方法为抽象方法，必须在子类中实现具体逻辑它提供了缓存系统初始化时的重要配置和定制选项，
     * 允许根据不同的缓存区域和属性灵活地设置缓存策略不同的缓存实现类可以根据给定的属性和区域，
     * 初始化最适合的缓存实例这个方法的存在使得缓存系统具有高度的 可配置性和灵活性，能够适应多种
     * 多样的缓存需求和场景
     */
    protected abstract CacheBuilder initCache(BaseCacheProperties cacheProperties, String cacheAreaWithPrefix);

    /**
     * 解析通用配置并应用到缓存构建器上
     *
     * @param builder         缓存构建器实例
     * @param cacheProperties 缓存属性配置
     */
    protected void parseGeneralConfig(CacheBuilder builder, BaseCacheProperties cacheProperties) {
        AbstractCacheBuilder<?> acb = (AbstractCacheBuilder) builder;

        acb.keyConvertor(new ParserFunction(getKeyConvertor(cacheProperties.getKeyConvertor())));

        Long expireAfterWriteInMillis = cacheProperties.getExpireAfterWriteInMillis();
        if (expireAfterWriteInMillis != null) {
            acb.setExpireAfterWriteInMillis(expireAfterWriteInMillis);
        }
        Long expireAfterAccessInMillis = cacheProperties.getExpireAfterAccessInMillis();
        if (expireAfterAccessInMillis != null) {
            acb.setExpireAfterAccessInMillis(expireAfterAccessInMillis);
        }

    }

    private String getKeyConvertor(KeyConvertorEnum keyConvertor) {
        if (Objects.isNull(keyConvertor)) {
            return KeyConvertor.GSON;
        }
        return keyConvertor.name();
    }

    @Autowired
    public void setAutoConfigureBeans(AutoConfigureBeans autoConfigureBeans) {
        this.autoConfigureBeans = autoConfigureBeans;
    }

    @Autowired
    public void setJetCacheProperties(JetCacheProperties jetCacheProperties) {
        this.jetCacheProperties = jetCacheProperties;
    }
}
