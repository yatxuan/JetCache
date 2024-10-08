package com.yat.cache.autoconfigure.utils;

import lombok.Getter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName ConfigTree
 * <p>Description ConfigTree</p>
 *
 * @author Yat
 * Date 2024/9/25 09:56
 * version 1.0
 */
public class ConfigTree {

    private final ConfigurableEnvironment environment;
    @Getter
    private final String prefix;

    public ConfigTree(ConfigurableEnvironment environment, String prefix) {
        Assert.notNull(environment, "environment is required");
        Assert.notNull(prefix, "prefix is required");
        this.environment = environment;
        this.prefix = prefix;
    }

    public ConfigTree subTree(String prefix) {
        return new ConfigTree(environment, fullPrefixOrKey(prefix));
    }

    private String fullPrefixOrKey(String prefixOrKey) {
        return this.prefix + prefixOrKey;
    }

    public String getProperty(String key, String defaultValue) {
        if (containsProperty(key)) {
            return getProperty(key);
        } else {
            return defaultValue;
        }
    }

    public boolean containsProperty(String key) {
        key = fullPrefixOrKey(key);
        return environment.containsProperty(key);
    }

    public String getProperty(String key) {
        key = fullPrefixOrKey(key);
        return environment.getProperty(key);
    }

    public boolean getProperty(String key, boolean defaultValue) {
        if (containsProperty(key)) {
            return Boolean.parseBoolean(getProperty(key));
        } else {
            return defaultValue;
        }
    }

    public int getProperty(String key, int defaultValue) {
        if (containsProperty(key)) {
            return Integer.parseInt(getProperty(key));
        } else {
            return defaultValue;
        }
    }

    public long getProperty(String key, long defaultValue) {
        if (containsProperty(key)) {
            return Long.parseLong(getProperty(key));
        } else {
            return defaultValue;
        }
    }

    public Set<String> directChildrenKeys() {
        Map<String, Object> m = getProperties();
        return m.keySet().stream().map(
                        s -> s.indexOf('.') >= 0 ? s.substring(0, s.indexOf('.')) : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> m = new HashMap<>();
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (source instanceof EnumerablePropertySource) {
                for (String name : ((EnumerablePropertySource<?>) source)
                        .getPropertyNames()) {
                    if (name != null && name.startsWith(prefix)) {
                        String subKey = name.substring(prefix.length());
                        m.put(subKey, environment.getProperty(name));
                    }
                }
            }
        }
        return m;
    }
}
