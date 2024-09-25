package com.yat.cache.core.event;

import com.yat.cache.core.JetCache;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName CacheEvent
 * <p>Description CacheEvent 用于单个 JVM 内部的缓存事件处理，而 CacheMessage 用于分布式环境下的消息传递。
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 12:03
 * version 1.0
 */
@Getter
@AllArgsConstructor
public class CacheEvent {

    /**
     * 缓存
     */
    protected JetCache jetCache;
}

