package com.yat.cache.core;

import com.yat.cache.core.exception.CacheInvokeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ClassName JetCache
 * <p>Description 缓存接口，支持空值</p>
 *
 * @author Yat
 * Date 2024/8/22 10:38
 * version 1.0
 */
public interface JetCache<K, V> extends Closeable {

    Logger logger = LoggerFactory.getLogger(JetCache.class);

    //-----------------------------JSR 107 style API------------------------------------------------

    /**
     * 从缓存中获取条目。
     * <p>如果缓存构建器指定了一个 {@link CacheLoader} 并且缓存中没有关联项，
     * 它将尝试加载该条目。</p>
     * <p>如果在访问缓存期间发生错误，方法返回 null 而不是抛出异常。</p>
     *
     * @param key 返回其关联值的键
     * @return 关联的值。null 可能表示：<ul>
     * <li>条目不存在或已过期</li>
     * <li>条目值为 null</li>
     * <li>访问缓存时发生错误（未抛出异常）</li>
     * </ul>
     * @throws CacheInvokeException 如果加载器抛出异常
     * @see CacheLoader
     * @see #GET(Object)
     */
    default V get(K key) throws CacheInvokeException {
        CacheGetResult<V> result = GET(key);
        if (result.isSuccess()) {
            return result.getValue();
        } else {
            return null;
        }
    }

    /**
     * 获取缓存值。
     * <p>如果实现支持异步操作，在此方法返回后缓存访问可能尚未完成。
     * 调用结果上的 `getResultCode()`、`isSuccess()`、`getMessage()` 或 `getValue()` 会阻塞直到缓存操作完成。
     * 调用 `future()` 方法可以获得一个 `CompletionStage` 实例用于异步编程。</p>
     *
     * @param key 键
     * @return 结果
     */
    CacheGetResult<V> GET(K key);

    /**
     * 从缓存中获取一系列条目，返回它们作为请求键集的值的映射。
     * <p>如果缓存构建器指定了一个 {@link CacheLoader} 并且缓存中没有关联项，
     * 它将尝试加载该条目。</p>
     * <p>如果在访问缓存期间发生错误，方法不会抛出异常。</p>
     *
     * @param keys 返回其关联值的键集
     * @return 找到给定键的条目映射。未在缓存中找到的键不在返回的映射中。
     * @throws CacheInvokeException 如果加载器抛出异常
     * @see CacheLoader
     * @see #GET_ALL(Set)
     */
    default Map<K, V> getAll(Set<? extends K> keys) throws CacheInvokeException {
        MultiGetResult<K, V> cacheGetResults = GET_ALL(keys);
        return cacheGetResults.unwrapValues();
    }

    /**
     * 获取所有缓存值。
     * <p>如果实现支持异步操作，在此方法返回后缓存访问可能尚未完成。
     * 调用结果上的 `getResultCode()`、`isSuccess()`、`getMessage()` 或 `getValue()` 会阻塞直到缓存操作完成。
     * 调用 `future()` 方法可以获得一个 `CompletionStage` 实例用于异步编程。</p>
     *
     * @param keys 键集合
     * @return 结果
     */
    MultiGetResult<K, V> GET_ALL(Set<? extends K> keys);

    /**
     * 将指定的值与指定的键关联到缓存中。
     * <p>如果在访问缓存期间发生错误，方法不会抛出异常。</p>
     * <p>如果实现支持异步操作，则该方法的缓存操作是异步的。</p>
     *
     * @param key   与指定值关联的键
     * @param value 与指定键关联的值
     * @see #PUT(Object, Object)
     */
    default void put(K key, V value) {
        PUT(key, value);
    }

    /**
     * 将指定的值与指定的键关联到缓存中。
     * <p>如果实现支持异步操作，则缓存访问可能在方法返回后未完成。调用结果上的 getResultCode()/isSuccess()/getMessage()
     * 方法将阻塞直到缓存操作完成。在结果上调用 future() 方法将得到一个用于异步编程的 CompletionStage 实例。</p>
     *
     * @param key   与指定值关联的键
     * @param value 与指定键关联的值
     * @return 结果
     */
    default CacheResult PUT(K key, V value) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT(key, value, config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 将指定的值与指定的键关联到缓存中。
     * <p>如果实现支持异步操作，则缓存访问可能在方法返回后未完成。调用结果上的 getResultCode()/isSuccess()/getMessage()
     * 方法将阻塞直到缓存操作完成。在结果上调用 future() 方法将得到一个用于异步编程的 CompletionStage 实例。</p>
     *
     * @param key              与指定值关联的键
     * @param value            与指定键关联的值
     * @param expireAfterWrite 关联的 KV 对的 TTL（存活时间）
     * @param timeUnit         expireAfterWrite 的时间单位
     * @return 结果
     */
    CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * 获取此缓存的配置.
     *
     * @return 缓存配置
     */
    CacheConfig<K, V> config();

    /**
     * 将指定映射中的所有条目复制到缓存中。
     * <p>如果在访问缓存期间发生错误，方法不会抛出异常。</p>
     * <p>如果实现支持异步操作，则该方法的缓存操作是异步的。</p>
     *
     * @param map 存储在缓存中的映射
     * @see #PUT_ALL(Map)
     */
    default void putAll(Map<? extends K, ? extends V> map) {
        PUT_ALL(map);
    }

    //--------------------------JetCache API---------------------------------------------

    /**
     * 将指定映射中的所有条目复制到缓存中。
     * <p>如果实现支持异步操作，则缓存访问可能在方法返回后未完成。调用结果上的 getResultCode()/isSuccess()/getMessage()
     * 方法将阻塞直到缓存操作完成。在结果上调用 future() 方法将得到一个用于异步编程的 CompletionStage 实例。</p>
     *
     * @param map 映射
     * @return 结果
     */
    default CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT_ALL(map, config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 放入所有缓存值。
     * <p>如果实现支持异步操作，则缓存访问可能在方法返回后未完成。调用结果上的 getResultCode()/isSuccess()/getMessage()
     * 方法将阻塞直到缓存操作完成。在结果上调用 future() 方法将得到一个用于异步编程的 CompletionStage 实例。</p>
     *
     * @param map              映射
     * @param expireAfterWrite 关联的 KV 对的 TTL（存活时间）
     * @param timeUnit         expireAfterWrite 的时间单位
     * @return 结果
     */
    CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * 如果指定的键尚未与值相关联，则原子性地将该键与给定的值相关联。
     * <p>如果在缓存访问期间发生错误，此方法不会抛出异常。</p>
     * <p>{@link MultiLevelJetCache}不支持此方法。</p>
     *
     * @param key   要与指定值相关联的键
     * @param value 要与指定键相关联的值
     * @return 如果设置了值，则返回true；如果键值对关联不存在于缓存中，或者在缓存访问期间发生错误，则返回false。
     * @see #PUT_IF_ABSENT(Object, Object, long, TimeUnit)
     */
    default boolean putIfAbsent(K key, V value) {
        // 使用默认的过期时间调用PUT_IF_ABSENT方法，并检查结果代码是否表示成功
        CacheResult result = PUT_IF_ABSENT(key, value, config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS);
        return result.getResultCode() == CacheResultCode.SUCCESS;
    }

    /**
     * 如果指定的键尚未与值相关联，则将其与给定的值相关联。
     * <p>如果实现支持异步操作，则缓存访问可能在方法返回后未完成。调用结果上的 getResultCode()/isSuccess()/getMessage()
     * 方法将阻塞直到缓存操作完成。在结果上调用 future() 方法将得到一个用于异步编程的 CompletionStage 实例。</p>
     *
     * @param key              键，用于标识缓存中的项。
     * @param value            值，要与键关联的缓存数据。
     * @param expireAfterWrite 写入后到期时间，表示缓存项在写入后的一段时间内将被 视为过期并可能被删除。
     * @param timeUnit         时间单位，用于指定 expireAfterWrite 的度量单位。
     * @return 返回一个包含操作结果和旧值（如果存在的话）的 CacheResult 对象。
     * 由于此操作是插入操作，旧值将为 null，因为这是针对不存在的键执行的操作。
     */
    CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * 如果缓存中存在指定键的映射，则移除该映射。
     * <p>如果在访问缓存过程中发生错误，该方法不会抛出异常。</p>
     *
     * @param key 需要从缓存中移除的键
     * @return 如果键成功移除则返回true，如果缓存中不存在键值关联或访问缓存时发生错误则返回false
     * @see #REMOVE(Object)
     */
    default boolean remove(K key) {
        return REMOVE(key).isSuccess();
    }

    /**
     * 如果存在，则从此缓存中移除键的映射。
     * <p>如果实现支持异步操作，则缓存访问可能在方法返回后未完成。调用结果上的 getResultCode()/isSuccess()/getMessage()
     * 方法将阻塞直到缓存操作完成。在结果上调用 future() 方法将得到一个用于异步编程的 CompletionStage 实例。</p>
     *
     * @param key 要从缓存中移除映射的键
     * @return 结果
     */
    CacheResult REMOVE(K key);

    /**
     * 移除指定键集中的条目。
     * <p>如果在访问缓存期间发生错误，方法不会抛出异常。</p>
     * <p>如果实现支持异步操作，则该方法的缓存操作是异步的。</p>
     *
     * @param keys 要移除的键集
     * @see #REMOVE_ALL(Set)
     */
    default void removeAll(Set<? extends K> keys) {
        REMOVE_ALL(keys);
    }

    /**
     * 移除指定键的所有条目。
     * <p>如果实现支持异步操作，则缓存访问可能在方法返回后未完成。调用结果上的 getResultCode()/isSuccess()/getMessage()
     * 方法将阻塞直到缓存操作完成。在结果上调用 future() 方法将得到一个用于异步编程的 CompletionStage 实例。</p>
     *
     * @param keys 要移除的键
     * @return 结果
     */
    CacheResult REMOVE_ALL(Set<? extends K> keys);

    /**
     * 提供了一种标准方式来访问底层具体的缓存条目实现，
     * 以便提供对进一步的专有功能的访问。
     * <p>
     * 如果实现不支持指定的类，
     * 则抛出 {@link IllegalArgumentException}。
     *
     * @param clazz 底层具体缓存的专有类或接口。返回的是此类。
     * @return 底层具体缓存的一个实例
     * @throws IllegalArgumentException 如果缓存提供程序不支持指定的类。
     */
    <T> T unwrap(Class<T> clazz);

    /**
     * 清理此缓存创建的资源.
     */
    @Override
    default void close() {
    }

    /**
     * 使用此缓存尝试独占地运行一个操作。
     * <p>{@link MultiLevelJetCache} 将使用最后一级缓存来支持此操作。</p>
     * 示例：
     * <pre>
     * cache.tryLock("MyKey", 100, TimeUnit.SECONDS), () -&gt; {
     *     // 执行某些操作
     * });
     * </pre>
     *
     * @param key      锁键
     * @param expire   锁过期时间
     * @param timeUnit 锁过期时间单位
     * @param action   需要执行的操作
     * @return 如果成功获取锁并执行了操作则返回 true
     */
    default boolean tryLockAndRun(K key, long expire, TimeUnit timeUnit, Runnable action) {
        try (AutoReleaseLock lock = tryLock(key, expire, timeUnit)) {
            if (lock != null) {
                action.run();
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 尝试获取指定键的独占锁，此方法不会阻塞。该方法适用于需要尝试获取锁但不希望因等待锁而延迟执行的场景。
     * <p>
     * 示例代码:
     * <pre>
     *   try(AutoReleaseLock lock = cache.tryLock("MyKey",100, TimeUnit.SECONDS)){
     *      if(lock != null){
     *          // do something
     *      }
     *   }
     * </pre>
     * <p>
     * 注意: {@link MultiLevelJetCache} 使用最后一级缓存来支持此操作。
     *
     * @param key      锁的键，不能为空。
     * @param expire   锁的过期时间。
     * @param timeUnit 锁过期时间的单位。
     * @return 成功时返回一个实现了java.lang.AutoCloseable的AutoReleaseLock实例，
     * 失败时返回null，表示其他线程/进程/服务器已持有锁，或者在缓存访问期间发生错误。
     * @see #tryLockAndRun(Object, long, TimeUnit, Runnable)
     */
    @SuppressWarnings("unchecked")
    default AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return null;
        }
        final String uuid = UUID.randomUUID().toString();
        final long expireTimestamp = System.currentTimeMillis() + timeUnit.toMillis(expire);
        final CacheConfig config = config();

        // 定义一个可自动释放锁的闭包
        AutoReleaseLock lock = () -> {
            int unlockCount = 0;
            while (unlockCount++ < config.getTryLockUnlockCount()) {
                if (System.currentTimeMillis() < expireTimestamp) {
                    CacheResult unlockResult = REMOVE(key);
                    if (unlockResult.getResultCode() == CacheResultCode.FAIL
                            || unlockResult.getResultCode() == CacheResultCode.PART_SUCCESS) {
                        logger.info("[tryLock] [{} of {}] [{}] unlock failed. Key={}, msg = {}",
                                unlockCount, config.getTryLockUnlockCount(), uuid, key, unlockResult.getMessage());
                        // retry
                    } else if (unlockResult.isSuccess()) {
                        logger.debug("[tryLock] [{} of {}] [{}] successfully release the lock. Key={}",
                                unlockCount, config.getTryLockUnlockCount(), uuid, key);
                        return;
                    } else {
                        logger.warn("[tryLock] [{} of {}] [{}] unexpected unlock result: Key={}, result={}",
                                unlockCount, config.getTryLockUnlockCount(), uuid, key, unlockResult.getResultCode());
                        return;
                    }
                } else {
                    logger.info("[tryLock] [{} of {}] [{}] lock already expired: Key={}",
                            unlockCount, config.getTryLockUnlockCount(), uuid, key);
                    return;
                }
            }
        };

        int lockCount = 0;
        JetCache jetCache = this;
        while (lockCount++ < config.getTryLockLockCount()) {
            CacheResult lockResult = jetCache.PUT_IF_ABSENT(key, uuid, expire, timeUnit);
            if (lockResult.isSuccess()) {
                logger.debug("[tryLock] [{} of {}] [{}] successfully get a lock. Key={}",
                        lockCount, config.getTryLockLockCount(), uuid, key);
                return lock;
            } else if (lockResult.getResultCode() == CacheResultCode.FAIL || lockResult.getResultCode() == CacheResultCode.PART_SUCCESS) {
                logger.info(
                        "[tryLock] [{} of {}] [{}] cache access failed during get lock, will inquiry {} times. " +
                                "Key={}, msg={}",
                        lockCount, config.getTryLockLockCount(), uuid, config.getTryLockInquiryCount(), key,
                        lockResult.getMessage());
                int inquiryCount = 0;
                while (inquiryCount++ < config.getTryLockInquiryCount()) {
                    CacheGetResult inquiryResult = jetCache.GET(key);
                    if (inquiryResult.isSuccess()) {
                        if (uuid.equals(inquiryResult.getValue())) {
                            logger.debug(
                                    "[tryLock] [{} of {}] [{}] successfully get a lock after inquiry. Key={}",
                                    inquiryCount, config.getTryLockInquiryCount(), uuid, key
                            );
                            return lock;
                        } else {
                            logger.debug(
                                    "[tryLock] [{} of {}] [{}] not the owner of the lock, return null. Key={}",
                                    inquiryCount, config.getTryLockInquiryCount(), uuid, key
                            );
                            return null;
                        }
                    } else {
                        logger.info(
                                "[tryLock] [{} of {}] [{}] inquiry failed. Key={}, msg={}",
                                inquiryCount, config.getTryLockInquiryCount(), uuid, key, inquiryResult.getMessage()
                        );
                        // retry inquiry
                    }
                }
            } else {
                // others holds the lock
                logger.debug(
                        "[tryLock] [{} of {}] [{}] others holds the lock, return null. Key={}",
                        lockCount, config.getTryLockLockCount(), uuid, key
                );
                return null;
            }
        }

        logger.debug("[tryLock] [{}] return null after {} attempts. Key={}", uuid, config.getTryLockLockCount(), key);
        return null;
    }

    /**
     * 如果键有关联的值，则返回该值；
     * 否则使用加载器加载值并返回，然后更新缓存。
     *
     * @param key    键
     * @param loader 值加载器
     * @return 值
     * @see CacheConfig#isCacheNullValue()
     */
    default V computeIfAbsent(K key, Function<K, V> loader) {
        return computeIfAbsent(key, loader, config().isCacheNullValue());
    }

    /**
     * 如果键有关联的值，则返回该值；
     * 否则使用加载器加载值并返回，然后更新缓存。
     *
     * @param key                           键
     * @param loader                        值加载器
     * @param cacheNullWhenLoaderReturnNull 如果加载器返回的 null 值应使用键存入缓存
     * @return 值
     */
    V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull);

    /**
     * 如果键已关联值，则返回该值；否则使用加载器加载值并返回，然后更新缓存。
     *
     * @param key                           键
     * @param loader                        值加载器
     * @param cacheNullWhenLoaderReturnNull 如果加载器返回 null 值是否应使用键放入缓存
     * @param expireAfterWrite              关联的 KV 对的 TTL（存活时间）
     * @param timeUnit                      expireAfterWrite 的时间单位
     * @return 值
     */
    V computeIfAbsent(
            K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull,
            long expireAfterWrite, TimeUnit timeUnit
    );

    /**
     * 将指定的值与指定的键关联到缓存中。
     * <p>如果在访问缓存时发生错误，方法不会抛出异常。</p>
     *
     * @param key              与指定值关联的键
     * @param value            与指定键关联的值
     * @param expireAfterWrite 关联的 KV 对的 TTL（存活时间）
     * @param timeUnit         expireAfterWrite 的时间单位
     * @see #PUT(Object, Object, long, TimeUnit)
     */
    default void put(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        PUT(key, value, expireAfterWrite, timeUnit);
    }

    /**
     * 将指定映射中的所有条目复制到缓存中。
     * <p>如果在访问缓存时发生错误，方法不会抛出异常。</p>
     *
     * @param map              要存储在此缓存中的映射
     * @param expireAfterWrite 关联的 KV 对的 TTL（存活时间）
     * @param timeUnit         expireAfterWrite 的时间单位
     * @see #PUT_ALL(Map, long, TimeUnit)
     */
    default void putAll(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        PUT_ALL(map, expireAfterWrite, timeUnit);
    }

}
