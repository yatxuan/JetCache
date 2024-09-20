package com.yat.cache.core;

import com.yat.cache.anno.api.CacheConsts;
import lombok.Setter;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.yat.cache.core.CacheResultCode.EXISTS;
import static com.yat.cache.core.CacheResultCode.FAIL;
import static com.yat.cache.core.CacheResultCode.PART_SUCCESS;
import static com.yat.cache.core.CacheResultCode.SUCCESS;

/**
 * ClassName CacheResult
 * <p>Description CacheResult类用于封装缓存操作的结果，提供了异步结果等待和结果状态查询的功能</p>
 * 该类是一个通用的缓存操作结果封装类，可以在不同的缓存操作场景中使用
 *
 * @author Yat
 * Date 2024/8/22 20:27
 * version 1.0
 */
public class CacheResult {
    /**
     * 默认的异步操作等待时间
     */
    private static Duration DEFAULT_TIMEOUT = CacheConsts.ASYNC_RESULT_TIMEOUT;
    /**
     * 非法参数错误信息
     */
    public static final String MSG_ILLEGAL_ARGUMENT = "illegal argument";
    /**
     * 成功但无具体消息的缓存操作结果
     */
    public static final CacheResult SUCCESS_WITHOUT_MSG = new CacheResult(SUCCESS, null);
    /**
     * 部分成功但无具体消息的缓存操作结果
     */
    public static final CacheResult PART_SUCCESS_WITHOUT_MSG = new CacheResult(PART_SUCCESS, null);
    /**
     * 失败但无具体消息的缓存操作结果
     */
    public static final CacheResult FAIL_WITHOUT_MSG = new CacheResult(FAIL, null);
    /**
     * 因非法参数导致失败的缓存操作结果
     */
    public static final CacheResult FAIL_ILLEGAL_ARGUMENT = new CacheResult(FAIL, MSG_ILLEGAL_ARGUMENT);
    /**
     * 操作对象已存在的缓存操作结果
     */
    public static final CacheResult EXISTS_WITHOUT_MSG = new CacheResult(EXISTS, null);
    /**
     * 封装了缓存操作结果的CompletableFuture
     */
    private final CompletionStage<ResultData> future;
    /**
     * 操作结果码
     */
    private volatile CacheResultCode resultCode;
    /**
     * 操作结果消息
     */
    private volatile String message;
    /**
     * 操作结果等待的超时时间
     */
    @Setter
    private volatile Duration timeout = DEFAULT_TIMEOUT;

    public CacheResult(CacheResultCode resultCode, String message) {
        this(CompletableFuture.completedFuture(new ResultData(resultCode, message, null)));
    }

    public CacheResult(CompletionStage<ResultData> future) {
        this.future = future;
    }

    public CacheResult(Throwable ex) {
        future = CompletableFuture.completedFuture(new ResultData(ex));
    }

    public boolean isSuccess() {
        return getResultCode() == SUCCESS;
    }

    public CacheResultCode getResultCode() {
        waitForResult();
        return resultCode;
    }

    protected void waitForResult() {
        waitForResult(timeout);
    }

    public void waitForResult(Duration timeout) {
        if (resultCode != null) {
            return;
        }
        try {
            ResultData resultData = future.toCompletableFuture().get(
                    timeout.toMillis(), TimeUnit.MILLISECONDS);
            fetchResultSuccess(resultData);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            fetchResultFail(e);
        }
    }

    protected void fetchResultSuccess(ResultData resultData) {
        message = resultData.getMessage();
        resultCode = resultData.getResultCode();
    }

    protected void fetchResultFail(Throwable e) {
        message = e.getClass() + ":" + e.getMessage();
        resultCode = FAIL;
    }

    public String getMessage() {
        waitForResult();
        return message;
    }

    public CompletionStage<ResultData> future() {
        return future;
    }

    /**
     * 设置默认的异步操作结果等待超时时间
     *
     * @param defaultTimeout 默认的超时时间
     */
    public static void setDefaultTimeout(Duration defaultTimeout) {
        DEFAULT_TIMEOUT = defaultTimeout;
    }
}
