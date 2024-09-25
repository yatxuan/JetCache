package com.yat.cache.anno.support;

import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.CacheManager;
import com.yat.cache.core.embedded.EmbeddedCacheBuilder;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.support.AbstractLifecycle;
import com.yat.cache.core.support.StatInfo;
import com.yat.cache.core.support.StatInfoLogger;
import com.yat.cache.core.template.CacheBuilderTemplate;
import com.yat.cache.core.template.CacheMonitorInstaller;
import com.yat.cache.core.template.MetricsMonitorInstaller;
import com.yat.cache.core.template.NotifyMonitorInstaller;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 缓存配置提供者类，负责初始化和管理缓存构建模板及监控安装器
 *
 * @author Yat
 * Date 2024/8/22 22:04
 * version 1.0
 */
public class ConfigProvider extends AbstractLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);
    /**
     * 性能指标回调
     */
    @Setter
    private Consumer<StatInfo> metricsCallback;
    /**
     * 缓存构建模板
     */
    @Getter
    private CacheBuilderTemplate cacheBuilderTemplate;
    /**
     * 全局缓存配置
     */
    @Setter
    @Getter
    protected GlobalCacheConfig globalCacheConfig;
    /**
     * 编码解析器
     */
    @Setter
    protected EncoderParser encoderParser;
    /**
     * 键转换器解析器
     */
    @Setter
    protected KeyConvertorParser keyConvertorParser;

    /**
     * 默认初始化解析器和回调
     */
    public ConfigProvider() {
        encoderParser = new DefaultEncoderParser();
        keyConvertorParser = new DefaultKeyConvertorParser();
        metricsCallback = new StatInfoLogger(false);
    }

    /**
     * 初始化缓存构建器模板和配置。
     * 根据全局缓存配置初始化缓存构建器，并处理本地和远程缓存构建器中的键转换器、值编码器和解码器。
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doInit() {
        // 初始化 CacheBuilderTemplate，设置穿透保护及本地和远程缓存构建器
        cacheBuilderTemplate = new CacheBuilderTemplate(
                globalCacheConfig.isPenetrationProtect(),
                globalCacheConfig.getLocalCacheBuilders(),
                globalCacheConfig.getRemoteCacheBuilders()
        );
        // 遍历并配置本地缓存构建器
        for (CacheBuilder builder : globalCacheConfig.getLocalCacheBuilders().values()) {
            EmbeddedCacheBuilder eb = (EmbeddedCacheBuilder) builder;
            // 如果键转换器是 ParserFunction 类型，则解析并设置新的键转换器
            if (eb.getConfig().getKeyConvertor() instanceof ParserFunction f) {
                eb.setKeyConvertor(parseKeyConvertor(f.value()));
            }
        }
        // 遍历并配置远程缓存构建器
        for (CacheBuilder builder : globalCacheConfig.getRemoteCacheBuilders().values()) {
            ExternalCacheBuilder eb = (ExternalCacheBuilder) builder;
            // 如果键转换器是 ParserFunction 类型，则解析并设置新的键转换器
            if (eb.getConfig().getKeyConvertor() instanceof ParserFunction f) {
                eb.setKeyConvertor(parseKeyConvertor(f.value()));
            }
            // 如果值编码器是 ParserFunction 类型，则解析并设置新的值编码器
            if (eb.getConfig().getValueEncoder() instanceof ParserFunction f) {
                eb.setValueEncoder(parseValueEncoder(f.value()));
            }
            // 如果值解码器是 ParserFunction 类型，则解析并设置新的值解码器
            if (eb.getConfig().getValueDecoder() instanceof ParserFunction f) {
                eb.setValueDecoder(parseValueDecoder(f.value()));
            }
        }
        // 初始化缓存监控安装器
        initCacheMonitorInstallers();
    }

    /**
     * 解析键转换器
     * <p>
     * 本方法旨在维持向后兼容性，允许外部根据字符串参数解析出一个键转换器函数
     * 注意：此处并无针对keyConvertorParser的getter方法
     *
     * @param convertor 描述了如何转换键的字符串
     * @return Function&lt;Object, Object&gt; 返回一个函数，该函数能够根据指定的转换规则对键进行转换
     */
    public Function<Object, Object> parseKeyConvertor(String convertor) {
        return keyConvertorParser.parseKeyConvertor(convertor);
    }


    /**
     * 解析值编码器:为值编码器字符串解析提供向后兼容性的方法
     * <p>
     * 该方法主要用于保持向后兼容性，允许外部通过字符串形式的值编码器来获取对应的编码功能
     * 注意：此方法没有提供获取encoderParser实例的getter方法，意味着外部无法直接访问或修改解析逻辑
     *
     * @param valueEncoder 描述了如何进行编码的字符串表示
     * @return 一个Function，接受一个对象并返回其编码后的字节数组
     */
    public Function<Object, byte[]> parseValueEncoder(String valueEncoder) {
        return encoderParser.parseEncoder(valueEncoder);
    }

    /**
     * 解析值解码器:为了向后兼容而保留此方法。
     * 注意：没有为 encoderParser 提供 getter 方法。
     *
     * @param valueDecoder 解析器的名称，用于解码值
     * @return 返回一个 Function 对象，该对象接受字节数组并返回解析后的对象
     */
    public Function<byte[], Object> parseValueDecoder(String valueDecoder) {
        return encoderParser.parseDecoder(valueDecoder);
    }

    /**
     * 初始化缓存监控安装器
     */
    protected void initCacheMonitorInstallers() {
        cacheBuilderTemplate.getCacheMonitorInstallers().add(metricsMonitorInstaller());
        cacheBuilderTemplate.getCacheMonitorInstallers().add(notifyMonitorInstaller());
        for (CacheMonitorInstaller i : cacheBuilderTemplate.getCacheMonitorInstallers()) {
            if (i instanceof AbstractLifecycle) {
                ((AbstractLifecycle) i).init();
            }
        }
    }

    /**
     * 创建并初始化性能指标监控安装器
     *
     * @return 初始化后的MetricsMonitorInstaller对象
     */
    protected CacheMonitorInstaller metricsMonitorInstaller() {
        Duration interval = null;
        if (globalCacheConfig.getStatIntervalMinutes() > 0) {
            interval = Duration.ofMinutes(globalCacheConfig.getStatIntervalMinutes());
        }

        MetricsMonitorInstaller i = new MetricsMonitorInstaller(metricsCallback, interval);
        i.init();
        return i;
    }

    /**
     * 创建通知监控安装器
     *
     * @return 初始化后的NotifyMonitorInstaller对象
     */
    protected CacheMonitorInstaller notifyMonitorInstaller() {
        return new NotifyMonitorInstaller(area -> globalCacheConfig.getRemoteCacheBuilders().get(area));
    }

    @Override
    public void doShutdown() {
        try {
            for (CacheMonitorInstaller i : cacheBuilderTemplate.getCacheMonitorInstallers()) {
                if (i instanceof AbstractLifecycle) {
                    ((AbstractLifecycle) i).shutdown();
                }
            }
        } catch (Exception e) {
            logger.error("close fail", e);
        }
    }

    public CacheNameGenerator createCacheNameGenerator(String[] hiddenPackages) {
        return new DefaultCacheNameGenerator(hiddenPackages);
    }

    public CacheContext newContext(CacheManager cacheManager) {
        return new CacheContext(cacheManager, this, globalCacheConfig);
    }

}
