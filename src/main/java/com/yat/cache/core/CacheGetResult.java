package com.yat.cache.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * ClassName CacheGetResult
 * <p>Description CacheGetResult 类用于表示缓存获取操作的结果</p>
 * 它扩展了 CacheResult 类,提供了关于缓存项是否存在、过期以及实际的值和持有者信息
 *
 * @author Yat
 * Date 2024/8/22 20:23
 * version 1.0
 */
public class CacheGetResult<V> extends CacheResult {
    /**
     * 表示缓存项不存在但没有附带消息的常量  to notExistsWithoutMsg()
     */
    @Deprecated
    public static final CacheGetResult NOT_EXISTS_WITHOUT_MSG =
            new CacheGetResult<>(CacheResultCode.NOT_EXISTS, null, null);
    /**
     * 表示缓存项已过期但没有附带消息的常量 to expiredWithoutMsg()
     */
    @Deprecated
    public static final CacheGetResult EXPIRED_WITHOUT_MSG = new CacheGetResult<>(CacheResultCode.EXPIRED, null, null);
    /**
     * volatile 修饰确保在多线程环境下对 value 的可见性
     */
    private volatile V value;
    /**
     * volatile 修饰确保在多线程环境下对 holder 的可见性
     */
    private volatile CacheValueHolder<V> holder;

    /**
     * 创建一个已完成的未来结果对象
     *
     * @param resultCode 结果代码，表示操作的结果
     * @param message    附带的消息，可能为 null
     * @param holder     缓存值的持有者，可能为 null
     */
    public CacheGetResult(CacheResultCode resultCode, String message, CacheValueHolder<V> holder) {
        super(CompletableFuture.completedFuture(new ResultData(resultCode, message, holder)));
    }

    /**
     * 创建一个指定阶段的未来结果对象
     *
     * @param future 表示结果的完成阶段
     */
    public CacheGetResult(CompletionStage<ResultData> future) {
        super(future);
    }

    /**
     * 创建一个表示异常的结果对象
     *
     * @param ex 表示失败原因的异常
     */
    public CacheGetResult(Throwable ex) {
        super(ex);
    }

    /**
     * 获取缓存的值
     */
    public V getValue() {
        waitForResult();
        return value;
    }

    /**
     * 当结果成功获取时的操作
     *
     * @param resultData 结果数据
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void fetchResultSuccess(ResultData resultData) {
        holder = (CacheValueHolder<V>) resultData.getOriginData();
        value = (V) unwrapValue(holder);
        super.fetchResultSuccess(resultData);
    }

    /**
     * 解包值
     *
     * @param holder 要解包的持有者对象
     * @return 解包后的值
     */
    static Object unwrapValue(Object holder) {
        // if @Cached or @CacheCache change type from REMOTE to BOTH (or from BOTH to REMOTE),
        // during the dev/publish process, the value type which different application server put into cache server
        // will be different
        // (CacheValueHolder<V> and CacheValueHolder<CacheValueHolder<V>>, respectively).
        // So we need correct the problem at here and in MultiLevelCache.unwrapHolder
        Object v = holder;
        // 循环解包，以处理值类型在不同应用服务器上的问题
        while (v instanceof CacheValueHolder) {
            v = ((CacheValueHolder) v).getValue();
        }
        return v;
    }

    /**
     * 当结果获取失败时的操作
     *
     * @param e 异常对象
     */
    @Override
    protected void fetchResultFail(Throwable e) {
        // 失败时将值设置为 null
        value = null;
        super.fetchResultFail(e);
    }

    /**
     * 获取缓存值的持有者
     *
     * @return 缓存值的持有者
     */
    protected CacheValueHolder<V> getHolder() {
        waitForResult();
        return holder;
    }

    /**
     * 表示缓存项不存在但没有附带消息的常量
     */
    public static <V> CacheGetResult<V> notExistsWithoutMsg() {
        return new CacheGetResult<>(CacheResultCode.NOT_EXISTS, null, null);
    }

    /**
     * 表示缓存项已过期但没有附带消息的常量
     */
    public static <V> CacheGetResult<V> expiredWithoutMsg() {
        return new CacheGetResult<>(CacheResultCode.EXPIRED, null, null);
    }
}
