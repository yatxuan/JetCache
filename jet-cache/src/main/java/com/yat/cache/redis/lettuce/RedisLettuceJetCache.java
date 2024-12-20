package com.yat.cache.redis.lettuce;

import com.yat.cache.core.CacheConfig;
import com.yat.cache.core.CacheGetResult;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.CacheResultCode;
import com.yat.cache.core.CacheValueHolder;
import com.yat.cache.core.MultiGetResult;
import com.yat.cache.core.ResultData;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.external.AbstractExternalJetCache;
import com.yat.cache.core.support.JetCacheExecutor;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.reactive.RedisClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ClassName RedisLettuceCache
 * <p>Description Redis Lettuce 数据缓存实现</p>
 *
 * @author Yat
 * Date 2024/9/20 23:09
 * version 1.0
 */
@SuppressWarnings("unchecked")
public class RedisLettuceJetCache<K, V> extends AbstractExternalJetCache<K, V> {

    private final RedisLettuceCacheConfig<K, V> config;

    private final Function<Object, byte[]> valueEncoder;
    private final Function<byte[], Object> valueDecoder;

    private final AbstractRedisClient client;
    private final LettuceConnectionManager lettuceConnectionManager;
    private final RedisStringCommands<byte[], byte[]> stringCommands;
    private final RedisStringAsyncCommands<byte[], byte[]> stringAsyncCommands;
    private final RedisKeyAsyncCommands<byte[], byte[]> keyAsyncCommands;

    public RedisLettuceJetCache(RedisLettuceCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();
        if (config.getRedisClient() == null) {
            throw new CacheConfigException("RedisClient is required");
        }
        if (config.isExpireAfterAccess()) {
            throw new CacheConfigException("expireAfterAccess is not supported");
        }

        client = config.getRedisClient();

        lettuceConnectionManager = config.getConnectionManager();
        lettuceConnectionManager.init(client, config.getConnection());
        stringCommands = (RedisStringCommands<byte[], byte[]>) lettuceConnectionManager.commands(client);
        stringAsyncCommands = (RedisStringAsyncCommands<byte[], byte[]>) lettuceConnectionManager.asyncCommands(client);
        keyAsyncCommands = (RedisKeyAsyncCommands<byte[], byte[]>) stringAsyncCommands;
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        if (AbstractRedisClient.class.isAssignableFrom(clazz)) {
            return (T) client;
        } else if (RedisClusterCommands.class.isAssignableFrom(clazz)) {
            // RedisCommands extends RedisClusterCommands
            return (T) stringCommands;
        } else if (RedisClusterAsyncCommands.class.isAssignableFrom(clazz)) {
            // RedisAsyncCommands extends RedisClusterAsyncCommands
            return (T) stringAsyncCommands;
        } else if (RedisClusterReactiveCommands.class.isAssignableFrom(clazz)) {
            // RedisReactiveCommands extends RedisClusterReactiveCommands
            return (T) lettuceConnectionManager.reactiveCommands(client);
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        try {
            byte[] newKey = buildKey(key);
            RedisFuture<byte[]> future = stringAsyncCommands.get(newKey);
            CacheGetResult<V> result = new CacheGetResult<>(future.handleAsync((valueBytes, ex) -> {
                if (ex != null) {
                    logError("GET", key, ex);
                    return new ResultData(ex);
                } else {
                    try {
                        if (valueBytes != null) {
                            CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(valueBytes);
                            if (System.currentTimeMillis() >= holder.getExpireTime()) {
                                return new ResultData(CacheResultCode.EXPIRED, null, null);
                            } else {
                                return new ResultData(CacheResultCode.SUCCESS, null, holder);
                            }
                        } else {
                            return new ResultData(CacheResultCode.NOT_EXISTS, null, null);
                        }
                    } catch (Exception exception) {
                        logError("GET", key, exception);
                        return new ResultData(exception);
                    }
                }
            }, JetCacheExecutor.defaultExecutor()));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("GET", key, ex);
            return new CacheGetResult<>(ex);
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        try {
            ArrayList<K> keyList = new ArrayList<>(keys);
            byte[][] newKeys = keyList.stream().map(this::buildKey).toArray(byte[][]::new);

            Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
            if (newKeys.length == 0) {
                return new MultiGetResult<>(CacheResultCode.SUCCESS, null, resultMap);
            }
            RedisFuture<List<KeyValue<byte[], byte[]>>> mGetResults = stringAsyncCommands.mget(newKeys);
            MultiGetResult<K, V> result = new MultiGetResult<>(mGetResults.handleAsync((list, ex) -> {
                if (ex != null) {
                    logError("GET_ALL", "keys(" + keys.size() + ")", ex);
                    return new ResultData(ex);
                } else {
                    try {
                        for (int i = 0; i < list.size(); i++) {
                            KeyValue<byte[], byte[]> kv = list.get(i);
                            K key = keyList.get(i);
                            if (kv != null && kv.hasValue()) {
                                CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(kv.getValue());
                                if (System.currentTimeMillis() >= holder.getExpireTime()) {
                                    resultMap.put(key, CacheGetResult.expiredWithoutMsg());
                                } else {
                                    CacheGetResult<V> r = new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder);
                                    resultMap.put(key, r);
                                }
                            } else {
                                resultMap.put(key, CacheGetResult.notExistsWithoutMsg());
                            }
                        }
                        return new ResultData(CacheResultCode.SUCCESS, null, resultMap);
                    } catch (Exception exception) {
                        logError("GET_ALL", "keys(" + keys.size() + ")", exception);
                        return new ResultData(exception);
                    }
                }
            }, JetCacheExecutor.defaultExecutor()));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<>(ex);
        }
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        try {
            CacheValueHolder<V> holder = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            RedisFuture<String> future = stringAsyncCommands.psetex(newKey, timeUnit.toMillis(expireAfterWrite),
                    valueEncoder.apply(holder));
            CacheResult result = new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("PUT", key, ex));
                    return new ResultData(ex);
                } else {
                    if ("OK".equals(rt)) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else {
                        return new ResultData(CacheResultCode.FAIL, rt, null);
                    }
                }
            }));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        try {
            CompletionStage<Integer> future = CompletableFuture.completedFuture(0);
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                CacheValueHolder<V> holder = new CacheValueHolder<>(en.getValue(), timeUnit.toMillis(expireAfterWrite));
                RedisFuture<String> resp = stringAsyncCommands.psetex(buildKey(en.getKey()),
                        timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
                future = future.thenCombine(resp, (failCount, respStr) -> "OK".equals(respStr) ? failCount :
                        failCount + 1);
            }
            CacheResult result = new CacheResult(future.handle((failCount, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("PUT_ALL", "map(" + map.size() + ")",
                            ex));
                    return new ResultData(ex);
                } else {
                    if (failCount == 0) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else if (failCount == map.size()) {
                        return new ResultData(CacheResultCode.FAIL, null, null);
                    } else {
                        return new ResultData(CacheResultCode.PART_SUCCESS, null, null);
                    }
                }
            }));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        try {
            RedisFuture<Long> future = keyAsyncCommands.del(buildKey(key));
            CacheResult result = new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("REMOVE", key, ex));
                    return new ResultData(ex);
                } else {
                    if (rt == null) {
                        return new ResultData(CacheResultCode.FAIL, null, null);
                    } else if (rt == 1) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else if (rt == 0) {
                        return new ResultData(CacheResultCode.NOT_EXISTS, null, null);
                    } else {
                        return new ResultData(CacheResultCode.FAIL, null, null);
                    }
                }
            }));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("REMOVE", key, ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        try {
            byte[][] newKeys = keys.stream().map(this::buildKey).toArray((len) -> new byte[keys.size()][]);
            RedisFuture<Long> future = keyAsyncCommands.del(newKeys);
            CacheResult result = new CacheResult(future.handle((v, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("REMOVE_ALL", "keys(" + keys.size() +
                            ")", ex));
                    return new ResultData(ex);
                } else {
                    return new ResultData(CacheResultCode.SUCCESS, null, null);
                }
            }));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        try {
            CacheValueHolder<V> holder = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            RedisFuture<String> future = stringAsyncCommands.set(newKey, valueEncoder.apply(holder),
                    SetArgs.Builder.nx().px(timeUnit.toMillis(expireAfterWrite)));
            CacheResult result = new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("PUT_IF_ABSENT", key, ex));
                    return new ResultData(ex);
                } else {
                    if ("OK".equals(rt)) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else if (rt == null) {
                        return new ResultData(CacheResultCode.EXISTS, null, null);
                    } else {
                        return new ResultData(CacheResultCode.FAIL, rt, null);
                    }
                }
            }));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("PUT_IF_ABSENT", key, ex);
            return new CacheResult(ex);
        }
    }

    private void setTimeout(CacheResult cr) {
        Duration d = Duration.ofMillis(config.getAsyncResultTimeoutInMillis());
        cr.setTimeout(d);
    }
}
