package com.yat.cache.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * MultiGetResult类用于封装批量获取操作的结果，继承自CacheResult
 * 它持有批量获取的每个键值对的获取结果
 *
 * @author Yat
 * Date 2024/8/21 下午11:16
 * version 1.0
 */
public class MultiGetResult<K, V> extends CacheResult {

    private volatile Map<K, CacheGetResult<V>> values;

    /**
     * 使用异步获取结果的Future对象初始化MultiGetResult
     *
     * @param future 包含结果数据的CompletionStage对象
     */
    public MultiGetResult(CompletionStage<ResultData> future) {
        super(future);
    }

    /**
     * 使用指定的结果码、消息和值映射初始化MultiGetResult
     *
     * @param resultCode 结果码，表示操作的结果
     * @param message    伴随结果码的消息
     * @param values     包含键值对获取结果的映射
     */
    public MultiGetResult(CacheResultCode resultCode, String message, Map<K, CacheGetResult<V>> values) {
        super(CompletableFuture.completedFuture(new ResultData(resultCode, message, values)));
    }

    /**
     * 当发生异常时使用
     *
     * @param e 异常对象
     */
    public MultiGetResult(Throwable e) {
        super(e);
    }

    /**
     * 获取所有键值对的获取结果映射
     *
     * @return 包含每个键值对获取结果的映射
     */
    public Map<K, CacheGetResult<V>> getValues() {
        waitForResult();
        return values;
    }

    /**
     * 当结果获取成功时处理结果数据
     *
     * @param resultData 包含结果的数据对象
     */
    @Override
    protected void fetchResultSuccess(ResultData resultData) {
        values = (Map<K, CacheGetResult<V>>) resultData.getOriginData();
        super.fetchResultSuccess(resultData);
    }

    /**
     * 当结果获取失败时处理异常
     *
     * @param e 导致失败的异常对象
     */
    @Override
    protected void fetchResultFail(Throwable e) {
        values = null;
        super.fetchResultFail(e);
    }

    /**
     * 解包获取结果，返回原始值的映射
     * 只包含获取成功的键值对
     *
     * @return 原始值的映射，只包含获取成功的键值对
     */
    public Map<K, V> unwrapValues() {
        waitForResult();
        if (values == null) {
            return null;
        }
        Map<K, V> m = new HashMap<>();
        values.forEach((key, value) -> {
            if (value.isSuccess()) {
                m.put(key, value.getValue());
            }
        });
        return m;
    }
}
