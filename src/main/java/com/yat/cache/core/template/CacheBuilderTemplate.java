package com.yat.cache.core.template;

import com.yat.cache.core.AbstractCacheBuilder;
import com.yat.cache.core.CacheBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName CacheBuilderTemplate
 * <p>Description 缓存构建器模板类，用于管理和配置多个级别的缓存构建器</p>
 *
 * @author Yat
 * Date 2024/8/22 12:50
 * version 1.0
 */
public class CacheBuilderTemplate {

    /**
     * 是否启用穿透保护。
     */
    @Getter
    private final boolean penetrationProtect;
    /**
     * 按级别组织的缓存构建器映射数组。 key: 缓存区域名称，value: 缓存构建器。
     */
    private final Map<String, CacheBuilder>[] cacheBuilders;
    /**
     * 缓存监控安装器列表。
     */
    @Getter
    private final List<CacheMonitorInstaller> cacheMonitorInstallers = new ArrayList<>();

    /**
     * 构造一个新的 CacheBuilderTemplate 实例。
     *
     * @param penetrationProtect 是否启用穿透保护。
     * @param cacheBuilders      按级别组织的缓存构建器映射数组。
     */
    @SafeVarargs
    public CacheBuilderTemplate(boolean penetrationProtect, Map<String, CacheBuilder>... cacheBuilders) {
        this.penetrationProtect = penetrationProtect;
        this.cacheBuilders = cacheBuilders;
    }

    /**
     * 获取指定级别的缓存构建器。
     *
     * @param level 级别索引。
     * @param area  缓存区域名称。
     * @return 返回指定级别的缓存构建器。
     */
    public CacheBuilder getCacheBuilder(int level, String area) {
        if (isEmpty()) {
            return null;
        }
        CacheBuilder cb = cacheBuilders[level].get(area);
        if (cb instanceof AbstractCacheBuilder) {
            return (CacheBuilder) ((AbstractCacheBuilder<?>) cb).clone();
        } else {
            return cb;
        }
    }

    /**
     * Description: 判断缓存构建器是否为空。
     * <p>
     * Date: 2024/8/28 09:27
     *
     * @return {@link boolean} 返回是否为空。true-空，false-非空
     */
    public boolean isEmpty() {
        for (Map<String, CacheBuilder> cacheBuilder : cacheBuilders) {
            if (Boolean.FALSE.equals(cacheBuilder.isEmpty())) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
}
