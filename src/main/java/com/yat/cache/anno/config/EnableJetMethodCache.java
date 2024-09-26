package com.yat.cache.anno.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用方法缓存的注解，用于在类级别开启缓存支持。
 * 通过此注解，可以配置缓存的一般行为，如代理模式、执行顺序等。
 *
 * @author Yat
 * Date 2024/8/22 21:14
 * version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CommonConfiguration.class, ConfigSelector.class})
public @interface EnableJetMethodCache {

    /**
     * 指定是否创建子类（CGLIB）代理而不是标准Java接口基于代理。
     * 默认值为false。仅当#mode()设置为AdviceMode.PROXY时适用。
     * <p>
     * 注意：将此属性设置为true会影响所有需要代理的Spring管理的bean，
     * 而不仅仅是那些标记为@Cacheable的bean。例如，标记为Spring的@Transactional注解的其他bean
     * 也将同时升级到子类代理。这种方法在实践中没有负面影响，除非明确期望某种类型的代理而不是另一种，
     * 例如在测试中。
     */
    boolean proxyTargetClass() default false;

    /**
     * 指定缓存通知应如何应用。默认值是{@link AdviceMode#PROXY}.
     *
     * @see AdviceMode
     */
    AdviceMode mode() default AdviceMode.PROXY;

    /**
     * 指定在多个通知应用于特定连接点时缓存通知的执行顺序.
     * 默认值是 {@link Ordered#LOWEST_PRECEDENCE}.
     */
    int order() default Ordered.LOWEST_PRECEDENCE;

    /**
     * 基础包名数组，指定需要扫描的包路径
     * <ul>
     *     <li>支持通配符，如：com.yat.*</li>
     *     <li>默认为空，表示扫描所有包</li>
     * </ul>
     */
    String[] basePackages() default {};

}
