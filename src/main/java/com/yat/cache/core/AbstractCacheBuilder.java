package com.yat.cache.core;

import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.exception.CacheException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ClassName AbstractCacheBuilder
 * <p>Description 构建缓存实例</p>
 *
 * @author Yat
 * Date 2024/8/22 11:39
 * version 1.0
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class AbstractCacheBuilder<T extends AbstractCacheBuilder<T>> implements CacheBuilder, Cloneable {

    /**
     * 构建函数，用于根据配置创建具体的缓存实例。
     */
    private Function<CacheConfig, Cache> buildFunc;
    /**
     * 缓存配置对象，用于存储缓存的各种配置信息。
     */
    protected CacheConfig config;

    /**
     * 设置构建函数，用于创建缓存实例
     */
    @SuppressWarnings("UnusedReturnValue")
    public T buildFunc(Function<CacheConfig, Cache> buildFunc) {
        this.buildFunc = buildFunc;
        return self();
    }

    /**
     * 获取当前缓存构建器自身实例，用于链式调用。todo 是否取消链式调用?
     */
    protected T self() {
        return (T) this;
    }

    /**
     * 构建缓存实例的方法
     * <p>
     * 根据缓存配置和构建函数来创建一个缓存实例 此方法为最终方法，不可重写
     *
     * @param <K> 缓存键的类型
     * @param <V> 缓存值的类型
     * @return 创建的缓存实例
     * @throws CacheConfigException 如果buildFunc未设置，则抛出此异常
     */
    @Override
    public final <K, V> Cache<K, V> buildCache() {
        // 检查构建函数是否已设置，如果未设置则抛出异常
        if (buildFunc == null) {
            throw new CacheConfigException("no buildFunc");
        }
        // 在构建缓存之前执行的操作
        beforeBuild();
        // 克隆当前缓存配置
        CacheConfig c = getConfig().clone();
        // 根据配置和构建函数创建基础缓存实例
        Cache<K, V> cache = buildFunc.apply(c);
        // 根据加载器和刷新策略对缓存进行包装，以提供额外功能
        if (c.getLoader() != null) {
            if (c.getRefreshPolicy() == null) {
                // 如果没有刷新策略，则包装为加载缓存
                cache = new LoadingCache<>(cache);
            } else {
                // 如果有刷新策略，则包装为刷新缓存
                cache = new RefreshCache<>(cache);
            }
        }
        // 返回最终的缓存实例
        return cache;
    }

    /**
     * 在构建缓存之前可以执行一些操作
     */
    protected void beforeBuild() {

    }

    /**
     * Description: 获取当前构建器的缓存配置。
     * <p>
     * Date: 2024/8/22 11:41
     *
     * @return {@link CacheConfig}
     */
    public abstract CacheConfig getConfig();

    /**
     * 克隆AbstractCacheBuilder实例的方法
     * <p>
     * 此方法通过覆盖Object类的clone方法来实现深拷贝
     * 它创建此AbstractCacheBuilder对象的一个复制品，包括其配置对象的复制品
     *
     * @return AbstractCacheBuilder的复制品
     * @throws CacheException 如果克隆操作失败
     */
    @Override
    public Object clone() {
        try {
            // 调用父类clone方法克隆当前对象
            AbstractCacheBuilder<?> copy = (AbstractCacheBuilder<?>) super.clone();
            // 克隆当前对象的配置对象，并将其赋给复制品
            copy.config = getConfig().clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            // 当克隆操作不被支持时，抛出CacheException
            throw new CacheException(e);
        }
    }

    /**
     * Description: 设置key转换器
     * <p>
     * Date: 2024/8/22 11:43
     *
     * @param keyConvertor key转换器
     */
    @SuppressWarnings("UnusedReturnValue")
    public T keyConvertor(Function<Object, Object> keyConvertor) {
        getConfig().setKeyConvertor(keyConvertor);
        return self();
    }

    /**
     * Description: 设置key转换器
     * <p>
     * Date: 2024/8/22 11:43
     *
     * @param keyConvertor key转换器
     */
    public void setKeyConvertor(Function<Object, Object> keyConvertor) {
        getConfig().setKeyConvertor(keyConvertor);
    }

    /**
     * Description: 设置访问后过期时间
     * <p>
     * Date: 2024/8/22 11:43
     *
     * @param defaultExpire 默认过期时间
     * @param timeUnit      时间单位
     */
    public T expireAfterAccess(long defaultExpire, TimeUnit timeUnit) {
        getConfig().setExpireAfterAccessInMillis(timeUnit.toMillis(defaultExpire));
        return self();
    }

    /**
     * Description: 设置访问后过期时间
     * <p>
     * Date: 2024/8/22 11:43
     *
     * @param expireAfterAccessInMillis 缓存过期时间
     */
    public void setExpireAfterAccessInMillis(long expireAfterAccessInMillis) {
        getConfig().setExpireAfterAccessInMillis(expireAfterAccessInMillis);
    }

    /**
     * Description: 设置写入后过期时间
     * <p>
     * Date: 2024/8/22 11:45
     *
     * @param defaultExpire 默认过期时间
     * @param timeUnit      时间单位
     */
    public T expireAfterWrite(long defaultExpire, TimeUnit timeUnit) {
        getConfig().setExpireAfterWriteInMillis(timeUnit.toMillis(defaultExpire));
        return self();
    }

    /**
     * Description: 设置写入后过期时间
     * <p>
     * Date: 2024/8/22 11:45
     *
     * @param expireAfterWriteInMillis 默认过期时间
     */
    public void setExpireAfterWriteInMillis(long expireAfterWriteInMillis) {
        getConfig().setExpireAfterWriteInMillis(expireAfterWriteInMillis);
    }

    /**
     * Description: 添加缓存监控器
     * <p>
     * Date: 2024/8/22 11:46
     *
     * @param monitor 监控器
     */
    public T addMonitor(CacheMonitor monitor) {
        getConfig().getMonitors().add(monitor);
        return self();
    }

    /**
     * Description: 设置缓存监控器
     * <p>
     * Date: 2024/8/22 11:46
     *
     * @param monitors 监控器
     */
    public void setMonitors(List<CacheMonitor> monitors) {
        getConfig().setMonitors(monitors);
    }

    /**
     * Description: 设置是否缓存空值
     * <p>
     * Date: 2024/8/22 11:46
     *
     * @param cacheNullValue 是否缓存空值
     */
    public T cacheNullValue(boolean cacheNullValue) {
        getConfig().setCacheNullValue(cacheNullValue);
        return self();
    }

    /**
     * Description: 设置是否缓存空值
     * <p>
     * Date: 2024/8/22 11:46
     *
     * @param cacheNullValue 是否缓存空值
     */
    public void setCacheNullValue(boolean cacheNullValue) {
        getConfig().setCacheNullValue(cacheNullValue);
    }

    /**
     * Description: 设置缓存加载器
     * <p>
     * Date: 2024/8/22 11:47
     *
     * @param loader 加载器
     */
    public <K, V> T loader(CacheLoader<K, V> loader) {
        getConfig().setLoader(loader);
        return self();
    }

    /**
     * Description: 设置缓存加载器
     * <p>
     * Date: 2024/8/22 11:47
     *
     * @param loader 加载器
     */
    public <K, V> void setLoader(CacheLoader<K, V> loader) {
        getConfig().setLoader(loader);
    }

    /**
     * Description: 设置刷新策略
     * <p>
     * Date: 2024/8/22 11:47
     *
     * @param refreshPolicy 刷新策略
     */
    public T refreshPolicy(RefreshPolicy refreshPolicy) {
        getConfig().setRefreshPolicy(refreshPolicy);
        return self();
    }

    /**
     * Description: 设置刷新策略
     * <p>
     * Date: 2024/8/22 11:47
     *
     * @param refreshPolicy 刷新策略
     */
    public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
        getConfig().setRefreshPolicy(refreshPolicy);
    }

    /**
     * 启用或禁用缓存穿透保护
     * 缓存穿透指的是在请求的数据在缓存和数据库中都不存在的情况下, 请求仍然不断地被发送到数据库, 从而对数据库造成压力
     * 通过启用缓存穿透保护, 可以在缓存中为不存在的数据设置一个空值, 以减少对数据库的请求
     *
     * @param cachePenetrateProtect 是否启用缓存穿透保护, true 表示启用, false 表示禁用
     * @return 返回当前实例, 以便进行链式调用
     */
    public T cachePenetrateProtect(boolean cachePenetrateProtect) {
        // 根据传入的参数, 设置缓存配置中的缓存穿透保护选项
        getConfig().setCachePenetrationProtect(cachePenetrateProtect);
        // 返回当前实例, 以便进行链式调用
        return self();
    }

    /**
     * Description: 设置是否启用缓存穿透保护
     * <p>
     * Date: 2024/8/22 11:47
     *
     * @param cachePenetrateProtect 是否启用缓存穿透保护
     */
    public void setCachePenetrateProtect(boolean cachePenetrateProtect) {
        getConfig().setCachePenetrationProtect(cachePenetrateProtect);
    }
}
