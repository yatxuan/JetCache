package com.yat.cache.redis.springdata;

import com.yat.cache.core.CacheGetResult;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.CacheResultCode;
import com.yat.cache.core.CacheValueHolder;
import com.yat.cache.core.MultiGetResult;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.external.AbstractExternalCache;
import com.yat.cache.core.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ClassName RedisSpringDataCache
 * <p>Description Redis Spring 数据缓存实现</p>
 *
 * @author Yat
 * Date 2024/8/22 22:12
 * version 1.0
 */
public class RedisSpringDataCache<K, V> extends AbstractExternalCache<K, V> {

    private final Logger logger = LoggerFactory.getLogger(RedisSpringDataCache.class);

    private final RedisConnectionFactory connectionFactory;
    private final RedisSpringDataCacheConfig<K, V> config;

    private final Function<Object, byte[]> valueEncoder;
    private final Function<byte[], Object> valueDecoder;

    public RedisSpringDataCache(RedisSpringDataCacheConfig<K, V> config) {
        super(config);
        this.connectionFactory = config.getConnectionFactory();
        Assert.notNull(connectionFactory, () -> new CacheConfigException("connectionFactory is required"));

        this.config = config;
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();
    }

    /**
     * 重写do_GET方法以执行缓存获取操作
     * 此方法专为Redis缓存设计，使用了Jedis客户端进行连接和操作
     *
     * @param key 缓存项的键，用于标识缓存数据
     * @return 返回CacheGetResult对象，其中包含了操作结果、可能的错误信息和缓存值
     */
    @Override
    protected CacheGetResult<V> do_GET(K key) {
        RedisConnection con = null;
        try {
            // 获取Redis连接
            con = connectionFactory.getConnection();
            // 构建Redis键的字节数组形式
            byte[] newKey = buildKey(key);
            // 尝试从Redis获取对应的值
            byte[] resultBytes = con.stringCommands().get(newKey);
            // 如果结果不为空
            if (resultBytes != null) {
                // 将结果字节转换为缓存值持有者对象
                CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(resultBytes);
                // 检查缓存项是否已过期
                if (System.currentTimeMillis() >= holder.getExpireTime()) {
                    // 如果已过期，返回过期结果
                    return CacheGetResult.expiredWithoutMsg();
                }
                // 返回成功获取缓存结果
                return new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder);
            } else {
                // 如果结果为空，返回不存在结果
                return CacheGetResult.notExistsWithoutMsg();
            }
        } catch (Exception ex) {
            // 如果发生异常，记录错误日志
            logError("GET", key, ex);
            // 返回包含异常的缓存获取结果
            return new CacheGetResult<>(ex);
        } finally {
            // 确保关闭Redis连接
            closeConnection(con);
        }
    }


    /**
     * 关闭Redis连接
     *
     * @param connection Redis连接对象
     */
    private void closeConnection(RedisConnection connection) {
        // 尝试关闭Redis连接，如果连接不为空
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            // 如果关闭连接时发生异常，则记录错误日志
            logger.error("RedisConnection close fail: {}, {}", ex.getMessage(), ex.getClass().getName());
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        RedisConnection con = null;
        try {
            // 获取Redis连接
            con = connectionFactory.getConnection();
            // 将键的集合转换为ArrayList，便于后续处理
            ArrayList<K> keyList = new ArrayList<>(keys);
            // 使用流式处理将键转换为Redis格式的键
            byte[][] newKeys = keyList.stream()
                    .map(this::buildKey)
                    .toArray(byte[][]::new);

            // 创建一个映射来存储键及其对应的获取结果
            Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
            // 如果有键需要查询，则执行mGet命令
            if (newKeys.length > 0) {
                // 使用Redis的mGet命令批量获取键对应的值
                List<byte[]> mGetResults = con.stringCommands().mGet(newKeys);
                // 如果获取结果非空，则处理每个键的获取结果
                if (Objects.nonNull(mGetResults)) {
                    for (int i = 0; i < mGetResults.size(); i++) {
                        // 获取当前键对应的值
                        Object value = mGetResults.get(i);
                        // 获取对应的键
                        K key = keyList.get(i);
                        // 如果值非空，则进一步处理
                        if (Objects.nonNull(value)) {
                            // 将获取到的值解码为CacheValueHolder对象
                            CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply((byte[]) value);
                            // 检查值是否已过期
                            if (System.currentTimeMillis() >= holder.getExpireTime()) {
                                // 如果已过期，将过期结果添加到结果映射中
                                resultMap.put(key, CacheGetResult.expiredWithoutMsg());
                            } else {
                                // 如果未过期，创建成功的CacheGetResult对象并添加到结果映射中
                                CacheGetResult<V> r = new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder);
                                resultMap.put(key, r);
                            }
                        } else {
                            // 如果值为空，表示键不存在，将不存在的结果添加到结果映射中
                            resultMap.put(key, CacheGetResult.notExistsWithoutMsg());
                        }
                    }
                }
            }
            // 返回包含所有键值对获取结果的对象
            return new MultiGetResult<>(CacheResultCode.SUCCESS, null, resultMap);
        } catch (Exception ex) {
            // 如果发生异常，记录错误日志，并返回包含异常的MultiGetResult对象
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<>(ex);
        } finally {
            // 关闭Redis连接
            closeConnection(con);
        }
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        // 声明Redis连接
        RedisConnection con = null;
        try {
            // 获取Redis连接
            con = connectionFactory.getConnection();
            // 构建缓存值持有者，包含值和过期时间
            CacheValueHolder<V> holder = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
            // 将键转换为字节数组
            byte[] keyBytes = buildKey(key);
            // 将值转换为字节数组并设置到Redis中
            byte[] valueBytes = valueEncoder.apply(holder);
            // 使用pSetEx命令设置键值对和过期时间
            Boolean result = con.stringCommands().pSetEx(keyBytes, timeUnit.toMillis(expireAfterWrite), valueBytes);
            // 根据操作结果返回成功或失败的缓存结果
            if (Boolean.TRUE.equals(result)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, "result:" + result);
            }
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(ex);
        } finally {
            closeConnection(con);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        // 声明Redis连接
        RedisConnection con = null;
        try {
            // 获取Redis连接
            con = connectionFactory.getConnection();
            long millis = timeUnit.toMillis(expireAfterWrite);
            // 失败计数器
            int failCount = 0;
            // 遍历键值对集合
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                // 构建缓存值持有者，包含值和过期时间
                CacheValueHolder<V> holder = new CacheValueHolder<>(en.getValue(), millis);
                // 设置键值对到Redis中
                Boolean result = con.stringCommands()
                        .pSetEx(buildKey(en.getKey()), millis, valueEncoder.apply(holder));
                // 如果设置失败，失败计数器增加
                if (!Boolean.TRUE.equals(result)) {
                    failCount++;
                }
            }
            // 根据失败和成功的数量返回相应的缓存结果
            return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                    failCount == map.size() ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            // 异常情况下，记录日志并返回异常结果
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        } finally {
            // 关闭Redis连接
            closeConnection(con);
        }
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        // 声明Redis连接
        RedisConnection con = null;
        try {
            // 获取Redis连接
            con = connectionFactory.getConnection();
            // 将键转换为字节数组
            byte[] keyBytes = buildKey(key);
            // 删除Redis中的键
            Long result = con.keyCommands().del(keyBytes);
            // 根据删除结果返回相应的缓存结果
            if (result == null) {
                return new CacheResult(CacheResultCode.FAIL, "result:" + null);
            } else if (result == 1) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else if (result == 0) {
                return new CacheResult(CacheResultCode.NOT_EXISTS, null);
            } else {
                return CacheResult.FAIL_WITHOUT_MSG;
            }
        } catch (Exception ex) {
            // 异常情况下，记录日志并返回异常结果
            logError("REMOVE", key, ex);
            return new CacheResult(ex);
        } finally {
            closeConnection(con);
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        // 声明Redis连接
        RedisConnection con = null;
        try {
            // 获取Redis连接
            con = connectionFactory.getConnection();
            // 将键集合转换为字节数组形式
            byte[][] newKeys = keys.stream()
                    .map(this::buildKey)
                    .toArray((len) -> new byte[keys.size()][]);
            // 删除Redis中的键集合
            Long result = con.keyCommands().del(newKeys);
            // 根据删除结果返回相应的缓存结果
            if (Objects.nonNull(result)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, "result:" + null);
            }
        } catch (Exception ex) {
            // 异常情况下，记录日志并返回异常结果
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
            return new CacheResult(ex);
        } finally {
            // 关闭Redis连接
            closeConnection(con);
        }
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        // 声明Redis连接
        RedisConnection con = null;
        try {
            // 获取Redis连接
            con = connectionFactory.getConnection();
            // 构建缓存值持有者，包含值和过期时间
            CacheValueHolder<V> holder = new CacheValueHolder<>(
                    value, timeUnit.toMillis(expireAfterWrite)
            );
            // 将键转换为字节数组
            byte[] newKey = buildKey(key);
            // 只有在键不存在时才设置值和过期时间
            Boolean result = con.stringCommands().set(
                    newKey,
                    valueEncoder.apply(holder),
                    Expiration.from(expireAfterWrite, timeUnit),
                    RedisStringCommands.SetOption.ifAbsent()
            );
            // 根据操作结果返回成功或存在的缓存结果
            if (Boolean.TRUE.equals(result)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            }/* else if (result == null) {
                return CacheResult.EXISTS_WITHOUT_MSG;
            } */ else {
                return CacheResult.EXISTS_WITHOUT_MSG;
            }
        } catch (Exception ex) {
            // 异常情况下，记录日志并返回异常结果
            logError("PUT_IF_ABSENT", key, ex);
            return new CacheResult(ex);
        } finally {
            // 关闭Redis连接
            closeConnection(con);
        }
    }

    @Override
    public RedisSpringDataCacheConfig<K, V> config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("RedisSpringDataCache does not support unwrap");
    }

}
