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
        return initExternalCache((RemoteCacheProperties) cacheProperties, cacheAreaWithPrefix);
    }

    /**
     * 初始化 远程 缓存的抽象方法
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
    protected abstract CacheBuilder initExternalCache(RemoteCacheProperties cacheProperties, String cacheAreaWithPrefix);

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

    protected String parseBroadcastChannel(RemoteCacheProperties cacheProperties) {
        String broadcastChannel = cacheProperties.getBroadcastChannel();
        if (broadcastChannel != null && !broadcastChannel.trim().isBlank()) {
            return broadcastChannel.trim();
        } else {
            return null;
        }
    }

}
