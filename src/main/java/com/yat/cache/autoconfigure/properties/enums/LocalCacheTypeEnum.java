package com.yat.cache.autoconfigure.properties.enums;

/**
 * ClassName LocalCacheTypeEnum
 * Description 本地缓存类型
 *
 * @author Yat
 * Date 2024/8/23 11:32
 * version 1.0
 */
public enum LocalCacheTypeEnum implements BaseCacheType {

    /**
     * linkedHashMap 缓存
     */
    LINKED_HASH_MAP,
    /**
     * caffeine 缓存
     */
    CAFFEINE;

    @Override
    public String getUpperName() {
        return this.name();
    }

}
