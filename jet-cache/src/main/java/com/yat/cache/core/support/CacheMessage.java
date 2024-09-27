package com.yat.cache.core.support;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * ClassName CacheMessage
 * <p>Description CacheMessage 用于分布式消息，而 CacheEvent 用于单个 JVM。</p>
 *
 * @author Yat
 * Date 2024/8/22 12:04
 * version 1.0
 */
@Setter
@Getter
public class CacheMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = -462475561129953207L;
    /**
     * 单个元素放入缓存的操作类型
     */
    public static final int TYPE_PUT = 1;
    /**
     * 批量元素放入缓存的操作类型
     */
    public static final int TYPE_PUT_ALL = 2;
    /**
     * 单个元素从缓存移除的操作类型
     */
    public static final int TYPE_REMOVE = 3;
    /**
     * 批量元素从缓存移除的操作类型
     */
    public static final int TYPE_REMOVE_ALL = 4;
    /**
     * 消息来源的唯一标识
     */
    private String sourceId;

    /**
     * 缓存区域标识
     */
    private String area;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 操作类型
     */
    private int type;

    /**
     * 操作涉及的键列表
     */
    private Object[] keys;

    /**
     * 操作涉及的值列表，保留字段
     */
    private Object[] values;

}
