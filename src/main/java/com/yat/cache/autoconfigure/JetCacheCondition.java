package com.yat.cache.autoconfigure;

import com.yat.cache.autoconfigure.properties.enums.BaseCacheType;
import com.yat.cache.autoconfigure.utils.JetCachePropertiesUtil;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName JetCacheCondition
 * <p>Description JetCache 缓存条件判断类</p>
 *
 * @author Yat
 * Date 2024/8/22 22:10
 * version 1.0
 */
public abstract class JetCacheCondition extends SpringBootCondition {

    private final String[] cacheTypes;

    protected JetCacheCondition(String... cacheTypes) {
        Objects.requireNonNull(cacheTypes, "cacheTypes can't be null");
        Assert.isTrue(cacheTypes.length > 0, "cacheTypes length is 0");
        this.cacheTypes = cacheTypes;
    }

    /**
     * 判断是否满足特定缓存条件
     * 本方法主要用于配置条件下判断是否启用了jetCache，通过检查特定的配置前缀来确定
     *
     * @param conditionContext      上下文信息，包含环境配置等
     * @param annotatedTypeMetadata 注解类型元数据，用于检查类型上的注解信息
     * @return ConditionOutcome 返回条件判断的结果，匹配或不匹配
     */
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext conditionContext,
                                            AnnotatedTypeMetadata annotatedTypeMetadata) {
        Set<String> cacheConfigTypes = getCacheConfig(conditionContext);

        if (cacheConfigTypes.isEmpty()) {
            return ConditionOutcome.noMatch("jetCache no config for");
        }
        if (match(cacheConfigTypes)) {
            return ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch("no match for " + this.cacheTypes[0]);
        }
    }

    /**
     * Description: 获取Yaml配置文件中的配置
     * <p>
     * Date: 2024/8/28 18:04
     *
     * @param context ConditionContext
     * @return {@link Set<BaseCacheType>}
     */
    private Set<String> getCacheConfig(ConditionContext context) {
        // Environment environment = context.getEnvironment(); // 获取 Environment
        ConfigurableEnvironment environment = (ConfigurableEnvironment) context.getEnvironment();
        return JetCachePropertiesUtil.getInstance(environment).getCacheConfigTypes()
                .stream()
                .map(BaseCacheType::getUpperName)
                .collect(Collectors.toSet());
    }

    /**
     * Description: 判断配置文件里面配置的类型是否和代码里面注入的类型进行匹配
     * <p>
     * Date: 2024/8/28 18:02
     *
     * @param cacheConfigTypes 配置文件里面配置的类型
     * @return {@link boolean}
     */
    private boolean match(Set<String> cacheConfigTypes) {
        return Arrays.stream(cacheTypes).anyMatch(cacheConfigTypes::contains);
    }
}
