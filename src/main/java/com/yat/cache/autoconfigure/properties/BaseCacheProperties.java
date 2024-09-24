package com.yat.cache.autoconfigure.properties;

import com.yat.cache.autoconfigure.properties.enums.KeyConvertorEnum;
import com.yat.cache.core.RefreshPolicy;
import lombok.Data;

import java.time.Duration;

/**
 * ClassName BaseCacheProperties
 * Description 基类-缓存信息
 *
 * @author Yat
 * Date 2024/8/23 11:47
 * version 1.0
 */
@Data
public abstract class BaseCacheProperties {

    /**
     * key转换器的全局配置 todo KeyConvertor
     */
    private KeyConvertorEnum keyConvertor = KeyConvertorEnum.NONE;

    /**
     * 是否缓存 null 值，默认为 false。
     */
    private Boolean cacheNullValue = Boolean.FALSE;
    /**
     * 穿透保护的有效期。
     */
    private Duration penetrationProtectTimeout = null;
    /**
     * 刷新策略，更新缓存条目的规则。
     */
    private RefreshPolicy refreshPolicy;

    /**
     * 写入后过期时间（毫秒），默认为 DEFAULT_EXPIRE * 1000 秒。
     */
    private Long expireAfterWriteInMillis = null;
    /**
     * 访问后过期时间（毫秒）
     */
    private Long expireAfterAccessInMillis = null;

    /**
     * 每个缓存实例的最大元素的全局配置
     */
    private Integer limit;
}
