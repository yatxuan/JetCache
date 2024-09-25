package com.yat.cache.anno.method;

/**
 * 定义了可用的表达语言类型的枚举。
 * 这些表达语言用于何种上下文中，由具体实现决定。
 * <p>
 * ClassName EL
 * <p>Description 表达语言类型的枚举</p>
 *
 * @author Yat
 * Date 2024/8/22 21:55
 * version 1.0
 */
enum EL {

    /**
     * 内置表达语言。
     */
    BUILD_IN,

    /**
     * MVEL表达语言。
     */
    MVEL,

    /**
     * Spring表达语言。
     */
    SPRING_EL
}
