package com.yat.cache.autoconfigure.properties.enums;

/**
 * ClassName RedisModeEnum
 * Description redis模式
 *
 * @author Yat
 * Date 2024/9/23 18:31
 * version 1.0
 */
public enum RedisModeEnum {
    /**
     * singleton-单机
     */
    SINGLETON,
    /**
     * cluster-集群
     */
    CLUSTER,
    /**
     * sentinel-哨兵模式
     */
    SENTINEL
}
