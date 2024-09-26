package com.yat.cache.autoconfigure.utils;

import com.yat.cache.autoconfigure.properties.JetCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.BaseCacheType;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * ClassName JetCachePropertiesUtil
 * Description 配置解析的工具类
 *
 * @author Yat
 * Date 2024/8/23 21:28
 * version 1.0
 */
@Getter
public class JetCachePropertiesUtil {

    private volatile static JetCachePropertiesUtil instance = null;
    private static final String pattern = ".*?.type";
    /**
     * 配置的类型集合
     */
    private final Set<BaseCacheType> cacheConfigTypes = new HashSet<>();

    private JetCachePropertiesUtil(ConfigurableEnvironment environment) {

        String prefix = getPrefix();
        Map<String, Object> systemProperties = environment.getSystemProperties();

        for (PropertySource<?> source : environment.getPropertySources()) {
            if (source instanceof EnumerablePropertySource<?> propertySource) {
                for (String name : propertySource.getPropertyNames()) {
                    if (name != null && name.startsWith(prefix)) {
                        String subKey = name.substring(prefix.length());
                        if (subKey.matches(pattern)) {
                            String property = environment.getProperty(name);
                            BaseCacheType baseCacheType = CacheTypeEnumRegistry.getREGISTRY().get(property);
                            if (Objects.nonNull(baseCacheType)) {
                                cacheConfigTypes.add(baseCacheType);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Description: 获取配置前缀
     * <p>
     * Date: 2024/8/28 16:17
     *
     * @return {@link String}
     */
    private static String getPrefix() {
        return JetCacheProperties.class
                .getAnnotation(ConfigurationProperties.class)
                .prefix();
    }

    public static JetCachePropertiesUtil getInstance(ConfigurableEnvironment configurableEnvironment) {
        if (Objects.isNull(instance)) {
            synchronized (JetCachePropertiesUtil.class) {
                if (Objects.isNull(instance)) {
                    instance = new JetCachePropertiesUtil(configurableEnvironment);
                }
            }
        }
        return instance;
    }
}
