package com.yat.cache.autoconfigure.properties.enums;

import lombok.NoArgsConstructor;

/**
 * ClassName RemoteCacheType
 * Description 远程缓存类型
 *
 * @author Yat
 * Date 2024/8/23 11:38
 * version 1.0
 */
@NoArgsConstructor
public enum RemoteCacheTypeEnum implements BaseCacheType {

    /**
     * redis-SpringData
     */
    REDIS_SPRING_DATA,
    /**
     * redis-lettuce
     */
    REDIS_LETTUCE,
    /**
     * mock
     */
    MOCK;

    @Override
    public String getUpperName() {
        return this.name();
    }
}
