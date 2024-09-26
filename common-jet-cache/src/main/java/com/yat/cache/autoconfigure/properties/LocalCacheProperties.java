package com.yat.cache.autoconfigure.properties;

import com.yat.cache.autoconfigure.properties.enums.LocalCacheTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ClassName LocalCacheProperties
 * Description 本地缓存信息
 *
 * @author Yat
 * Date 2024/8/23 11:48
 * version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LocalCacheProperties extends BaseCacheProperties {

    /**
     * 缓存类型
     */
    private LocalCacheTypeEnum type;

}
