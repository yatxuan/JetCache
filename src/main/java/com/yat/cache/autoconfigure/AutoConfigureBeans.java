package com.yat.cache.autoconfigure;

import com.yat.cache.core.CacheBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName AutoConfigureBeans
 * <p>Description 自动配置缓存 bean</p>
 *
 * @author Yat
 * Date 2024/8/22 22:09
 * version 1.0
 */
@Setter
@Getter
public class AutoConfigureBeans {

    private Map<String, CacheBuilder> localCacheBuilders = new HashMap<>();

    private Map<String, CacheBuilder> remoteCacheBuilders = new HashMap<>();

    private Map<String, Object> customContainer = Collections.synchronizedMap(new HashMap<>());

}
