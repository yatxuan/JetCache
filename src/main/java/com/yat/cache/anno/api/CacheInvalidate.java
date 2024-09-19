package com.yat.cache.anno.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName JetCacheInvalidate
 * <p>Description 用于标记需要从缓存中移除数据的方法。</p>
 * <p>
 *     todo 重命名 JetCacheRemove
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 09:48
 * version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CacheInvalidateContainer.class)
@Target(ElementType.METHOD)
public @interface CacheInvalidate {


    /**
     * 如果你想使用多后端缓存系统，你可以在配置中设置多个“缓存区域”，
     * 这个属性指定了你想使用的“缓存区域”的名称。
     *
     * @return 缓存区域的名称
     */
    String area() default CacheConsts.DEFAULT_AREA;


    /**
     * 需要执行移除操作的缓存实例的名称。
     *
     * @return 需要执行移除操作的缓存名称
     */
    String name();

    /**
     * 通过表达式脚本指定键，可选。如果没有指定，
     * 则使用目标方法的所有参数和键转换器来生成一个键。
     *
     * @return 指定键的表达式脚本
     */
    String key() default CacheConsts.UNDEFINED_STRING;

    /**
     * 用于条件控制缓存操作的表达式脚本，当评估结果为 `false` 时，将阻止缓存操作。
     * 评估发生在实际方法调用之后，因此可以在脚本中引用 `<code>#result</code>`。
     *
     * @return 控制缓存操作的条件表达式脚本
     */
    String condition() default CacheConsts.UNDEFINED_STRING;

    /**
     * 如果评估后的键是一个数组或实现了 `java.lang.Iterable` 接口的实例，
     * 设置 `multi` 为 `true` 表示对每个可迭代键中的元素都进行无效化操作。
     *
     * @return 是否对可迭代键中的每个元素进行无效化操作
     */
    boolean multi() default CacheConsts.DEFAULT_MULTI;
}
