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

    /**
     * 解析通用缓存配置
     * 此方法用于根据RemoteCacheProperties配置来配置ExternalCacheBuilder
     *
     * @param builder    缓存构建器，在此案例中是ExternalCacheBuilder的实例
     * @param properties 缓存属性对象，实际类型为RemoteCacheProperties
     */
    @Override
    protected void parseGeneralConfig(CacheBuilder builder, BaseCacheProperties properties) {
        // 调用父类的同名方法处理通用配置
        super.parseGeneralConfig(builder, properties);

        ExternalCacheBuilder ecb = (ExternalCacheBuilder) builder;
        RemoteCacheProperties remoteCacheProperties = (RemoteCacheProperties) properties;

        // 设置缓存键的前缀，使用键转换器的名称
        ecb.setKeyPrefix(remoteCacheProperties.getKeyPrefix());
        // 解析并设置广播通道
        ecb.setBroadcastChannel(parseBroadcastChannel(remoteCacheProperties));

        // 默认序列化策略
        String value = DefaultCacheConstant.DEFAULT_SERIAL_POLICY;
        // 如果指定了序列化策略类型，则使用它
        SerialPolicyTypeEnum serialPolicyTypeEnum = remoteCacheProperties.getSerialPolicyTypeEnum();
        if (Objects.nonNull(serialPolicyTypeEnum)) {
            value = serialPolicyTypeEnum.name();
        }

        // 创建一个解析函数，用于根据序列化策略解析数据
        ParserFunction parserFunction = new ParserFunction(value);
        // 设置值编码器和解码器为同一解析函数
        ecb.setValueEncoder(parserFunction);
        ecb.setValueDecoder(parserFunction);
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
