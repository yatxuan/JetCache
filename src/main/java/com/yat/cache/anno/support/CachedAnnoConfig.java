package com.yat.cache.anno.support;

import com.yat.cache.anno.api.CacheType;
import com.yat.cache.core.RefreshPolicy;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ClassName CachedAnnoConfig
 * <p>Description 注解缓存配置</p>
 *
 * @author Yat
 * Date 2024/8/20 11:25
 * version 1.0
 */
@Setter
@Getter
public class CachedAnnoConfig extends CacheAnnoConfig {
    /**
     * 是否启用缓存
     */
    private boolean enabled;
    /**
     * 指定expire的单位
     */
    private TimeUnit timeUnit;
    /**
     * 超时时间。如果注解上没有定义，会使用全局配置，如果此时全局配置也没有定义，则为无穷大
     */
    private long expire;
    /**
     * 仅当 {@link #cacheType} 为 {@link CacheType#BOTH} 时适用，
     * 为内存中的Cache指定一个不一样的超时时间，通常应该小于expire
     */
    private long localExpire;
    /**
     * 缓存的类型，包括 {@link CacheType#REMOTE}、{@link CacheType#LOCAL}、{@link CacheType#BOTH}。
     * 如果定义为BOTH，会使用LOCAL和REMOTE组合成两级缓存
     */
    private CacheType cacheType;
    /**
     * 是否同步更新本地缓存，默认异步。
     */
    private boolean syncLocal;
    /**
     * 如果cacheType为LOCAL或BOTH，这个参数指定本地缓存的最大元素数量，以控制内存占用。
     * 如果注解上没有定义，会使用全局配置，
     * 如果此时全局配置也没有定义，则为100
     */
    private int localLimit;
    /**
     * 是否缓存空值，如果注解上没有定义，会使用全局配置，
     */
    private boolean cacheNullValue;
    /**
     * 指定远程缓存的序列化方式。
     * 可选值为 JAVA、KRYO、 KRYO5、GSON
     * 如果注解上没有定义，会使用全局配置，
     * 如果此时全局配置也没有定义，则为 JAVA
     */
    private String serialPolicy;
    /**
     * 指定KEY的转换方式，用于将复杂的KEY类型转换为缓存实现可以接受的类型，
     * 当前支持 GSON、JACKSON、NONE
     * NONE表示不转换，
     * GSON可以将复杂对象KEY转换成String。
     * 如果注解上没有定义，会使用全局配置。
     */
    private String keyConvertor;
    /**
     * 使用SpEL表达式指定条件，表达式返回true时更新缓存。评估在方法执行后进行。
     * <p></p>
     * <a href="https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/expressions.html">SpEL</a>
     */
    private String postCondition;
    /**
     * Post condition评估器，用于评估postCondition表达式。
     */
    private Function<Object, Boolean> postConditionEvaluator;
    /**
     * 缓存刷新策略。
     */
    private RefreshPolicy refreshPolicy;
    /**
     * 穿透保护配置。
     */
    private PenetrationProtectConfig penetrationProtectConfig;


}
