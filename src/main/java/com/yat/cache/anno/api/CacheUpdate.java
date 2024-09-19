package com.yat.cache.anno.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName CacheUpdate
 * <p>Description 用于配置缓存更新操作</p>
 *<p>
 *     todo 重命名 JetCacheUpdate
 *</p>
 * @author Yat
 * Date 2024/8/22 09:54
 * version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheUpdate {

    /**
     * 如果你想使用多后端缓存系统，你可以在配置中设置多个“缓存区域”，
     * 这个属性指定了你想使用的“缓存区域”的名称。
     *
     * @return 缓存区域的名称
     */
    String area() default CacheConsts.DEFAULT_AREA;

    /**
     * 需要更新操作的此缓存实例的名称
     *
     * @return 需要更新操作的此缓存实例的名称
     */
    String name();

    /**
     * 通过表达式脚本指定键，可选。如果未指定，请使用目标方法和 keyConvertor 的所有参数来生成一个.
     *
     * @return 指定键的表达式脚本
     */
    String key() default CacheConsts.UNDEFINED_STRING;

    /**
     * 通过表达式脚本指定缓存值.
     *
     * @return 指定缓存值的表达式脚本
     */
    String value();

    /**
     * 用于条件控制缓存操作的表达式脚本，当评估结果为 `false` 时，将阻止缓存操作。
     * 评估发生在实际方法调用之后，因此可以在脚本中引用 `<code>#result</code>`。
     *
     * @return 控制缓存操作的条件表达式脚本
     */
    String condition() default CacheConsts.UNDEFINED_STRING;

    /**
     * 如果评估后的键和值都是数组或实现了 `java.lang.Iterable` 接口的实例，
     * 设置 `multi` 为 `true` 表示 cache 将更新 K/V 对到缓存，而不是更新单个转换后的 K/V。
     *
     * @return 是否对可迭代键和值进行批量更新
     */
    boolean multi() default CacheConsts.DEFAULT_MULTI;
}
