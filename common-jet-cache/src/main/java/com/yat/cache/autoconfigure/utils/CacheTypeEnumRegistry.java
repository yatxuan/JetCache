package com.yat.cache.autoconfigure.utils;

import com.yat.cache.autoconfigure.properties.enums.BaseCacheType;
import com.yat.cache.autoconfigure.properties.enums.LocalCacheTypeEnum;
import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName CacheTypeEnumRegistry
 * Description 缓存类型-枚举注册
 *
 * @author Yat
 * Date 2024/8/28 16:31
 * version 1.0
 */
@Slf4j
@UtilityClass
public class CacheTypeEnumRegistry {

    @Getter
    private static final Map<String, BaseCacheType> REGISTRY = new ConcurrentHashMap<>();

    static {
        //  注册
        register(LocalCacheTypeEnum.class);
        register(RemoteCacheTypeEnum.class);
    }

    private static synchronized void register(Class<? extends BaseCacheType> clazz) {
        BaseCacheType[] baseCacheTypes = clazz.getEnumConstants();
        if (Objects.nonNull(baseCacheTypes)) {
            for (BaseCacheType baseCacheType : baseCacheTypes) {
                REGISTRY.put(baseCacheType.getUpperName().toUpperCase(), baseCacheType);
                REGISTRY.put(baseCacheType.getUpperName().toLowerCase(), baseCacheType);
            }
        }
    }

}
