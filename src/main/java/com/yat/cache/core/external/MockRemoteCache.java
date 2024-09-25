package com.yat.cache.core.external;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheConfig;
import com.yat.cache.core.CacheGetResult;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.CacheValueHolder;
import com.yat.cache.core.MultiGetResult;
import com.yat.cache.core.embedded.LinkedHashMapCacheBuilder;
import com.yat.cache.core.exception.CacheException;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName MockRemoteCache
 * <p>Description 模拟远程缓存实现</p>
 * <p> 此类模拟了一个远程缓存的行为，实际上使用本地内存作为存储。 </p>
 *
 * @author Yat
 * Date 2024/8/22 13:43
 * version 1.0
 */
public class MockRemoteCache<K, V> extends AbstractExternalCache<K, V> {
    private static final Method getHolder;

    static {
        try {
            getHolder = CacheGetResult.class.getDeclaredMethod("getHolder");
            getHolder.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new CacheException(e);
        }
    }

    private final Cache<ByteBuffer, byte[]> cache;
    private final ExternalCacheConfig<K, V> config;

    public MockRemoteCache(MockRemoteCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(config.getLimit())
                .expireAfterWrite(config.getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS)
                .buildCache();
    }

    //-------------------------------

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return cache.unwrap(clazz);
    }

    /**
     * 获取指定键的 CacheValueHolder。
     *
     * @param key 键
     * @return CacheValueHolder 对象
     */
    public CacheValueHolder getHolder(K key) {
        try {
            CacheGetResult<V> r = GET(key);
            return (CacheValueHolder) getHolder.invoke(r);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /**
     * 获取单个键的缓存结果。
     *
     * @param key 键
     * @return 缓存结果
     */
    @Override
    protected CacheGetResult<V> do_GET(K key) {
        CacheGetResult r = cache.GET(genKey(key));
        if (r.isSuccess()) {
            r = convertCacheGetResult(r);
        }
        return r;
    }

    /**
     * 将键转换为 ByteBuffer 形式。
     *
     * @param key 键
     * @return ByteBuffer 形式的键
     */
    private ByteBuffer genKey(K key) {
        return ByteBuffer.wrap(buildKey(key));
    }

    /**
     * 转换 CacheGetResult 结果。
     *
     * @param originResult 原始结果
     * @return 转换后的结果
     */
    private CacheGetResult convertCacheGetResult(CacheGetResult originResult) {
        try {
            CacheValueHolder originHolder = (CacheValueHolder) getHolder.invoke(originResult);
            LinkedList<CacheValueHolder> list = new LinkedList<>();
            while (originHolder != null) {
                CacheValueHolder h = new CacheValueHolder();
                if (!list.isEmpty()) {
                    list.getLast().setValue(h);
                }
                list.add(h);
                h.setAccessTime(originHolder.getAccessTime());
                h.setExpireTime(originHolder.getExpireTime());

                Object v = originHolder.getValue();
                if (v != null && !(v instanceof CacheValueHolder)) {
                    h.setValue(config.getValueDecoder().apply((byte[]) v));
                    break;
                } else if (originHolder.getValue() == null) {
                    originHolder = null;
                }
            }
            return new CacheGetResult<>(
                    originResult.getResultCode(), originResult.getMessage(), list.peekFirst()
            );
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        ArrayList<K> keyList = new ArrayList<>(keys.size());
        ArrayList<ByteBuffer> newKeyList = new ArrayList<>(keys.size());
        keys.forEach((k) -> {
            ByteBuffer newKey = genKey(k);
            keyList.add(k);
            newKeyList.add(newKey);
        });
        MultiGetResult<ByteBuffer, byte[]> result = cache.GET_ALL(new HashSet(newKeyList));
        Map<ByteBuffer, CacheGetResult<byte[]>> resultMap = result.getValues();
        if (resultMap != null) {
            Map<K, CacheGetResult<V>> returnMap = new HashMap<>();
            for (int i = 0; i < keyList.size(); i++) {
                K key = keyList.get(i);
                ByteBuffer newKey = newKeyList.get(i);
                CacheGetResult r = resultMap.get(newKey);
                if (r.getValue() != null) {
                    r = convertCacheGetResult(r);
                }
                returnMap.put(key, r);
            }
            result = new MultiGetResult<ByteBuffer, byte[]>(result.getResultCode(), null, (Map) returnMap);
        }
        return (MultiGetResult) result;
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        return cache.PUT(genKey(key), config.getValueEncoder().apply(value), expireAfterWrite, timeUnit);
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        Map<ByteBuffer, byte[]> newMap = new HashMap<>();
        map.forEach((key, value) -> newMap.put(genKey(key), config.getValueEncoder().apply(value)));
        return cache.PUT_ALL(newMap, expireAfterWrite, timeUnit);
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        return cache.REMOVE(genKey(key));
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        return cache.REMOVE_ALL(keys.stream().map(this::genKey).collect(Collectors.toSet()));
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        return cache.PUT_IF_ABSENT(genKey(key), config.getValueEncoder().apply(value), expireAfterWrite, timeUnit);
    }
}
