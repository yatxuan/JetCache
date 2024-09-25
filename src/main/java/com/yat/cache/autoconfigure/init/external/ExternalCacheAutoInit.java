package com.yat.cache.autoconfigure.init.external;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.support.ParserFunction;
import com.yat.cache.autoconfigure.init.AbstractCacheAutoInit;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
import com.yat.cache.autoconfigure.properties.RemoteCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.external.ExternalCacheBuilder;

import java.util.Objects;

/**
 * ClassName ExternalCacheAutoInit
 * <p>Description 远程缓存</p>
 *
 * @author Yat
 * Date 2024/8/22 22:10
 * version 1.0
 */
public abstract class ExternalCacheAutoInit extends AbstractCacheAutoInit {

    public ExternalCacheAutoInit(String... cacheTypes) {
        super(cacheTypes);
    }

    @Override
    protected CacheBuilder initCache(BaseCacheProperties cacheProperties, String cacheAreaWithPrefix) {
        RemoteCacheProperties remoteCacheProperties = (RemoteCacheProperties) cacheProperties;

        // 获取缓存构建器
        ExternalCacheBuilder<?> builder = createExternalCacheBuilder(
                remoteCacheProperties, cacheAreaWithPrefix
        );
        // 解析通用配置
        parseExternalGeneralConfig(builder, remoteCacheProperties);
        // 后置逻辑
        afterExternalCacheInit(builder, remoteCacheProperties, cacheAreaWithPrefix);
        return builder;
    }

    /**
     * 创建一个嵌入式缓存构建器
     *
     * @return 返回一个嵌入式缓存构建器实例，具体类型视实现而定
     */
    protected abstract ExternalCacheBuilder<?> createExternalCacheBuilder(
            RemoteCacheProperties cacheProperties, String cacheAreaWithPrefix
    );

    /**
     * Description: 解析远程缓存通用配置
     * <p>
     * Date: 2024/9/25 15:59
     *
     * @param builder    缓存构建器
     * @param properties 缓存配置
     */
    @SuppressWarnings({"unchecked"})
    protected void parseExternalGeneralConfig(ExternalCacheBuilder<?> builder, RemoteCacheProperties properties) {
        // 调用父类的同名方法处理通用配置
        super.parseGeneralConfig(builder, properties);

        // 设置缓存键的前缀，使用键转换器的名称
        builder.setKeyPrefix(properties.getKeyPrefix());
        // 解析并设置广播通道
        builder.setBroadcastChannel(parseBroadcastChannel(properties));

        // 默认序列化策略
        String valueEncoder = DefaultCacheConstant.DEFAULT_SERIAL_POLICY;
        // 指定序列化策略类型
        SerialPolicyTypeEnum encoder = properties.getValueEncoder();
        if (Objects.nonNull(encoder)) {
            valueEncoder = encoder.name();
        }

        String valueDecoder = DefaultCacheConstant.DEFAULT_SERIAL_POLICY;
        SerialPolicyTypeEnum decoder = properties.getValueDecoder();
        if (Objects.nonNull(decoder)) {
            valueDecoder = decoder.name();
        }

        // 创建一个解析函数，用于根据序列化策略解析数据
        builder.setValueEncoder(new ParserFunction(valueEncoder));
        builder.setValueDecoder(new ParserFunction(valueDecoder));
    }

    /**
     * Description: 缓存初始化后，执行一些后置逻辑
     * <p>
     * Date: 2024/9/25 15:52
     *
     * @param builder         缓存构建器
     * @param cacheProperties 缓存配置
     */
    protected void afterExternalCacheInit(
            ExternalCacheBuilder<?> builder, RemoteCacheProperties cacheProperties, String cacheAreaWithPrefix
    ) {
    }

    protected String parseBroadcastChannel(RemoteCacheProperties cacheProperties) {
        String broadcastChannel = cacheProperties.getBroadcastChannel();
        if (broadcastChannel != null && !broadcastChannel.trim().isBlank()) {
            return broadcastChannel.trim();
        } else {
            return null;
        }
    }

}
