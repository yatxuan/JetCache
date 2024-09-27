package com.yat.cache.core.template;

import com.yat.cache.anno.api.CacheType;
import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.CacheLoader;
import com.yat.cache.core.RefreshPolicy;
import lombok.Getter;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * ClassName QuickConfig
 * <p>Description 快速配置类，用于缓存操作的简化配置</p>
 *
 * @author Yat
 * Date 2024/8/22 20:14
 * version 1.0
 */
public class QuickConfig {

    /**
     * 如果在配置中配置了多个缓存区域，在这里指定使用哪个区域
     */
    @Getter
    private String area = DefaultCacheConstant.DEFAULT_AREA;
    /**
     * 缓存名称
     * 指定缓存的唯一名称，不是必须的，
     * 如果没有指定，会使用类名+方法名。
     * name会被用于远程缓存的key前缀。
     * 另外在统计中，一个简短有意义的名字会提高可读性。
     */
    @Getter
    private String name;
    /**
     * 远程缓存过期时间设置
     */
    @Getter
    private Duration expire;
    /**
     * 本地缓存过期时间设置
     */
    @Getter
    private Duration localExpire;
    /**
     * 如果cacheType为LOCAL或BOTH，这个参数指定本地缓存的最大元素数量，以控制内存占用。
     * 如果注解上没有定义，会使用全局配置，如果此时全局配置也没有定义，则为100
     */
    @Getter
    private Integer localLimit;
    /**
     * 缓存的类型，包括CacheType.REMOTE、CacheType.LOCAL、CacheType.BOTH。
     * 如果定义为BOTH，会使用LOCAL和REMOTE组合成两级缓存
     */
    @Getter
    private CacheType cacheType;
    /**
     * 当缓存类型为BOTH时，远程缓存更新时是否同步更新本地缓存
     */
    @Getter
    private Boolean syncLocal;
    /**
     * 键转换函数，用于缓存键的转换
     */
    @Getter
    private Function<Object, Object> keyConvertor;
    /**
     * 值编码函数，将缓存值编码为字节数组
     */
    @Getter
    private Function<Object, byte[]> valueEncoder;
    /**
     * 值解码函数，将字节数组解码为缓存值
     */
    @Getter
    private Function<byte[], Object> valueDecoder;
    /**
     * 是否缓存空值
     */
    @Getter
    private Boolean cacheNullValue;
    /**
     * 此属性控制是否将区域 {@link QuickConfig#area} 添加为远程缓存键前缀 拼接到 {@link QuickConfig#name} 前面
     */
    @Getter
    private Boolean useAreaInPrefix;
    /**
     * 是否启用防穿透保护
     */
    @Getter
    private Boolean penetrationProtect;
    /**
     * 防穿透保护的缓存时间
     */
    @Getter
    private Duration penetrationProtectTimeout;
    /**
     * 刷新策略，用于远程缓存更新时的配置
     */
    @Getter
    private RefreshPolicy refreshPolicy;
    /**
     * 缓存加载器，用于缓存数据的加载
     */
    private CacheLoader<? extends Object, ? extends Object> loader;

    private QuickConfig() {
    }

    public <K, V> CacheLoader<K, V> getLoader() {
        return (CacheLoader<K, V>) loader;
    }

    public static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static Builder newBuilder(String area, String name) {
        return new Builder(area, name);
    }

    public static class Builder {
        /**
         * 如果在配置中配置了多个缓存区域，在这里指定使用哪个区域
         */
        private String area = DefaultCacheConstant.DEFAULT_AREA;
        /**
         * 缓存名称
         * 指定缓存的唯一名称，不是必须的，
         * 如果没有指定，会使用类名+方法名。
         * name会被用于远程缓存的key前缀。
         * 另外在统计中，一个简短有意义的名字会提高可读性。
         */
        private final String name;
        /**
         * 远程缓存过期时间设置
         */
        private Duration expire;
        /**
         * 本地缓存过期时间设置
         */
        private Duration localExpire;
        /**
         * 如果cacheType为LOCAL或BOTH，这个参数指定本地缓存的最大元素数量，以控制内存占用。
         * 如果注解上没有定义，会使用全局配置，如果此时全局配置也没有定义，则为100
         */
        private Integer localLimit;
        /**
         * 缓存的类型，包括CacheType.REMOTE、CacheType.LOCAL、CacheType.BOTH。
         * 如果定义为BOTH，会使用LOCAL和REMOTE组合成两级缓存
         */
        private CacheType cacheType;
        /**
         * 当缓存类型为BOTH时，远程缓存更新时是否同步更新本地缓存
         */
        private Boolean syncLocal;
        /**
         * 键转换函数，用于缓存键的转换
         */
        private Function<Object, Object> keyConvertor;
        /**
         * 值编码函数，将缓存值编码为字节数组
         */
        private Function<Object, byte[]> valueEncoder;
        /**
         * 值解码函数，将字节数组解码为缓存值
         */
        private Function<byte[], Object> valueDecoder;
        /**
         * 是否缓存空值
         */
        private Boolean cacheNullValue;
        /**
         * 此属性控制是否将区域 {@link QuickConfig#area} 添加为远程缓存键前缀 拼接到 {@link QuickConfig#name} 前面
         */
        private Boolean useAreaInPrefix;
        /**
         * 是否启用防穿透保护
         */
        private Boolean penetrationProtect;
        /**
         * 防穿透保护的缓存时间
         */
        private Duration penetrationProtectTimeout;
        /**
         * 刷新策略，用于远程缓存更新时的配置
         */
        private RefreshPolicy refreshPolicy;
        /**
         * 缓存加载器，用于缓存数据的加载
         */
        private CacheLoader<? extends Object, ? extends Object> loader;

        Builder(String name) {
            Objects.requireNonNull(name);
            this.name = name;
        }

        Builder(String area, String name) {
            Objects.requireNonNull(area);
            Objects.requireNonNull(name);
            this.area = area;
            this.name = name;
        }

        public QuickConfig build() {
            QuickConfig c = new QuickConfig();
            c.area = area;
            c.name = name;
            c.expire = expire;
            c.localExpire = localExpire;
            c.localLimit = localLimit;
            c.cacheType = cacheType;
            c.syncLocal = syncLocal;
            c.keyConvertor = keyConvertor;
            c.valueEncoder = valueEncoder;
            c.valueDecoder = valueDecoder;
            c.cacheNullValue = cacheNullValue;
            c.useAreaInPrefix = useAreaInPrefix;
            c.penetrationProtect = penetrationProtect;
            c.penetrationProtectTimeout = penetrationProtectTimeout;
            c.refreshPolicy = refreshPolicy;
            c.loader = loader;
            return c;
        }

        public Builder expire(Duration expire) {
            this.expire = expire;
            return this;
        }

        public Builder localExpire(Duration localExpire) {
            this.localExpire = localExpire;
            return this;
        }

        public Builder localLimit(Integer localLimit) {
            this.localLimit = localLimit;
            return this;
        }

        public Builder cacheType(CacheType cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public Builder syncLocal(Boolean syncLocal) {
            this.syncLocal = syncLocal;
            return this;
        }

        public Builder keyConvertor(Function<Object, Object> keyConvertor) {
            this.keyConvertor = keyConvertor;
            return this;
        }

        public Builder valueEncoder(Function<Object, byte[]> valueEncoder) {
            this.valueEncoder = valueEncoder;
            return this;
        }

        public Builder valueDecoder(Function<byte[], Object> valueDecoder) {
            this.valueDecoder = valueDecoder;
            return this;
        }

        public Builder cacheNullValue(Boolean cacheNullValue) {
            this.cacheNullValue = cacheNullValue;
            return this;
        }

        public Builder useAreaInPrefix(Boolean useAreaInPrefix) {
            this.useAreaInPrefix = useAreaInPrefix;
            return this;
        }

        public Builder penetrationProtect(Boolean penetrationProtect) {
            this.penetrationProtect = penetrationProtect;
            return this;
        }

        public Builder penetrationProtectTimeout(Duration penetrationProtectTimeout) {
            this.penetrationProtectTimeout = penetrationProtectTimeout;
            return this;
        }

        public Builder refreshPolicy(RefreshPolicy refreshPolicy) {
            this.refreshPolicy = refreshPolicy;
            return this;
        }

        public <K, V> Builder loader(CacheLoader<K, V> loader) {
            this.loader = loader;
            return this;
        }
    }
}
