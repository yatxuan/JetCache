package com.yat.cache.anno.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * ClassName JetCached
 * <p>Description 缓存注解</p>
 *
 * <p>
 *     todo 重命名 JetCached
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 09:30
 * version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {

    /**
     * 如果你想使用多后端缓存系统，你可以在配置中设置多个“缓存区域”，
     * 这个属性指定了你想使用的“缓存区域”的名称。
     *
     * @return 缓存区域的名称
     */
    String area() default CacheConsts.DEFAULT_AREA;

    /**
     * 此缓存实例的名称，可选。如果不指定，JetCache 将自动生成一个。
     * 该名称用于显示统计信息，并在使用远程缓存时用作键前缀的一部分。
     * 不要为具有相同区域的不同 {@link Cached} 注释分配相同的名称。
     *
     * @return 缓存的名称
     */
    String name() default CacheConsts.UNDEFINED_STRING;

    /**
     * 指定是否启用方法缓存。
     * 如果设置为 false，则可以在线程上下文中使用
     * {@code CacheContext.enableCache(Supplier<T> callback)}
     *
     * @return 如果启用了方法缓存
     */
    boolean enabled() default CacheConsts.DEFAULT_ENABLED;

    /**
     * 指定过期时间的时间单位.(默认:秒)
     *
     * @return 过期时间的时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 缓存过期时间。如果未指定此属性值，则使用全局配置，
     * 如果也没有定义全局配置，则使用无限期缓存
     *
     * @return 缓存过期时间
     */
    int expire() default CacheConsts.UNDEFINED_INT;

    /**
     * 当缓存类型为 {@link CacheType#BOTH} 时，指定本地缓存的过期时间。
     * 如果未指定，则使用 expire 属性
     *
     * @return 本地缓存的过期时间
     */
    int localExpire() default CacheConsts.UNDEFINED_INT;

    /**
     * 缓存实例的类型。可能是 {@link CacheType#REMOTE}、{@link CacheType#LOCAL} 或 {@link CacheType#BOTH}。
     * 当值为 {@link CacheType#BOTH} 时，创建一个两级缓存（本地 + 远程）。*
     *
     * @return 方法缓存的缓存类型
     */
    CacheType cacheType() default CacheType.REMOTE;

    /**
     * 如果缓存类型为 {@link CacheType#BOTH} 并且远程缓存在支持广播（或存在 BroadcastManager Bean）的情况下，
     * 在执行 put/remove 操作后使所有进程中的本地缓存失效。
     *
     * @return 是否应同步本地缓存
     */
    boolean syncLocal() default false;

    /**
     * 当缓存类型为 LOCAL 或 BOTH 时，指定本地内存中的最大元素数量。
     * 如果未指定此属性值，则使用全局配置，
     * 如果也没有定义全局配置，则使用 {@link CacheConsts#DEFAULT_LOCAL_LIMIT}。
     *
     * @return local maximal elements of the LOCAL/BOTH cache
     */
    int localLimit() default CacheConsts.UNDEFINED_INT;

    /**
     * 当缓存类型为 REMOTE 或 BOTH 时，指定远程缓存的序列化策略。
     * JetCache 内置的序列化策略包括 SerialPolicy.JAVA 和 SerialPolicy.KRYO。
     * 如果未指定此属性值，则使用全局配置，
     * 如果也没有定义全局配置，则使用 SerialPolicy.JAVA。
     *
     * @return 缓存值的序列化策略名称
     */
    String serialPolicy() default CacheConsts.UNDEFINED_STRING;

    /**
     * 指定键转换器。用于转换复杂的键对象。
     * JetCache 内置的键转换器包括 KeyConvertor.FASTJSON 和 KeyConvertor.NONE。 NONE 表示不进行转换，
     * FASTJSON 将使用 fastjson 将键对象转换为字符串。
     * 如果未指定此属性值，则使用全局配置
     *
     * @return 缓存键的转换器名称
     */
    String keyConvertor() default CacheConsts.UNDEFINED_STRING;

    /**
     * 通过表达式脚本指定键，可选。
     * 如果没有指定， 则使用目标方法的所有参数和键转换器来生成一个键。
     *
     * @return 指定键的表达式脚本
     */
    String key() default CacheConsts.UNDEFINED_STRING;

    /**
     * 指定是否应该缓存空值.
     *
     * @return 是否应该缓存空值
     */
    boolean cacheNullValue() default CacheConsts.DEFAULT_CACHE_NULL_VALUE;

    /**
     * 用于条件控制方法缓存的表达式脚本，在方法真正调用之前评估。
     * 如果评估结果为 false，则不使用缓存。
     *
     * @return 控制方法缓存的条件表达式脚本。
     */
    String condition() default CacheConsts.UNDEFINED_STRING;

    /**
     * 用于条件控制方法缓存更新的表达式脚本，在方法真正调用之后评估。
     * 如果评估结果为 false，则阻止缓存更新操作。
     * 评估时可以引用 result。
     *
     * @return 控制方法缓存更新的条件表达式脚本
     */
    String postCondition() default CacheConsts.UNDEFINED_STRING;

}
