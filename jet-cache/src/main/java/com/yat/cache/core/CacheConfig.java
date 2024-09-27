package com.yat.cache.core;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.exception.CacheException;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * ClassName CacheConfig
 * <p>Description 单独的 缓存配置类:配置缓存的行为特性，</p>
 *
 * @author Yat
 * Date 2024/8/22 10:57
 * version 1.0
 */
@Setter
@Getter
public class CacheConfig<K, V> implements Cloneable {

    /**
     * 写入后过期时间（毫秒），默认为 DEFAULT_EXPIRE * 1000 毫秒。
     */
    private long expireAfterWriteInMillis = DefaultCacheConstant.DEFAULT_EXPIRE * 1000L;
    /**
     * 访问后过期时间（毫秒），默认为 0。
     */
    private long expireAfterAccessInMillis = 0;
    /**
     * 键转换器，用于转换缓存键。
     */
    private Function<K, Object> keyConvertor;
    /**
     * 缓存加载器，指定如何加载缓存值。
     */
    private CacheLoader<K, V> loader;
    /**
     * 监控器列表，用于跟踪缓存操作。
     */
    private List<CacheMonitor> monitors = new ArrayList<>();
    /**
     * 是否缓存 null 值，默认为 false。
     */
    private boolean cacheNullValue = false;
    /**
     * 刷新策略，更新缓存条目的规则。
     */
    private RefreshPolicy refreshPolicy;

    /**
     * 尝试获取锁和释放锁的次数，默认为 2 次。
     */
    private int tryLockUnlockCount = 2;

    /**
     * 尝试获取锁前的查询次数，默认为 1 次。
     */
    private int tryLockInquiryCount = 1;

    /**
     * 尝试获取锁的次数，默认为 2 次。
     */
    private int tryLockLockCount = 2;

    /**
     * 缓存渗透保护
     */
    private boolean cachePenetrationProtect = false;
    /**
     * 防穿透保护的缓存时间
     */
    private Duration penetrationProtectTimeout = null;

    @Override
    public CacheConfig clone() {
        try {
            CacheConfig copy = (CacheConfig) super.clone();
            if (monitors != null) {
                copy.monitors = new ArrayList<>(this.monitors);
            }
            if (refreshPolicy != null) {
                copy.refreshPolicy = this.refreshPolicy.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    /**
     * 判断是否有访问后过期设置。
     *
     * @return 如果访问后有过期设置则返回 true，否则返回 false。
     */
    public boolean isExpireAfterAccess() {
        return expireAfterAccessInMillis > 0;
    }

    /**
     * 判断是否有写入后过期设置。
     *
     * @return 如果写入后有过期设置则返回 true，否则返回 false。
     */
    public boolean isExpireAfterWrite() {
        return expireAfterWriteInMillis > 0;
    }

    @Deprecated
    public long getDefaultExpireInMillis() {
        return expireAfterWriteInMillis;
    }

    @Deprecated
    public void setDefaultExpireInMillis(long defaultExpireInMillis) {
        this.expireAfterWriteInMillis = defaultExpireInMillis;
    }

}
