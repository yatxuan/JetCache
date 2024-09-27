package com.yat.cache.autoconfigure.properties;

import com.yat.cache.autoconfigure.properties.enums.ReadFromEnum;
import com.yat.cache.autoconfigure.properties.enums.RedisModeEnum;
import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
import com.yat.cache.autoconfigure.properties.redis.RedisPropertiesConfig;
import com.yat.cache.autoconfigure.properties.redis.lettuce.ClusterPropertiesConfig;
import com.yat.cache.autoconfigure.properties.redis.lettuce.SentinelPropertiesConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * ClassName RemoteCacheProperties
 * Description 远程缓存配置
 *
 * @author Yat
 * Date 2024/8/23 11:49
 * version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RemoteCacheProperties extends BaseCacheProperties {

    /**
     * 缓存类型
     */
    private RemoteCacheTypeEnum type;
    /**
     * 键前缀
     */
    private String keyPrefix;
    /**
     * 序列化器的全局配置
     */
    private SerialPolicyTypeEnum valueEncoder;
    private SerialPolicyTypeEnum valueDecoder;
    /**
     * 广播通道名称
     * 其他JVM中的local cache，但多个服务共用redis同一个channel可能会造成广播风暴，
     * 需要在这里指定channel，你可以决定多个不同的服务是否共用同一个channel。如果没有指定则不开启。
     */
    private String broadcastChannel;

    @NestedConfigurationProperty
    private RedisDataProperties redisData;
    @NestedConfigurationProperty
    private RedisLettuceProperties lettuce;

    @Data
    public static class RedisDataProperties {
        /**
         * redis连接工厂
         */
        private String connectionFactory;

    }

    @Data
    public static class RedisLettuceProperties {
        private RedisModeEnum mode = RedisModeEnum.SINGLETON;
        /**
         * 数据读取策略
         */
        private ReadFromEnum readFrom;
        private Long asyncResultTimeoutInMillis;
        private Integer enablePeriodicRefresh;
        private Boolean enableAllAdaptiveRefreshTriggers;

        @NestedConfigurationProperty
        private RedisPropertiesConfig singleton;
        @NestedConfigurationProperty
        private SentinelPropertiesConfig sentinel;
        @NestedConfigurationProperty
        private ClusterPropertiesConfig cluster;
    }
}
