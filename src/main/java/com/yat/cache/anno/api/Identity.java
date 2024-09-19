package com.yat.cache.anno.api;

/**
 * ClassName Identity
 * Description 序列化的标识号
 *
 * @author Yat
 * Date 2024/8/22 10:11
 * version 1.0
 */
public interface Identity {

    /**
     * Java 序列化的标识号
     */
    int IDENTITY_NUMBER_JAVA = 0x4A953A80;
    /**
     * Kryo 4.x 版本的标识号
     */
    int IDENTITY_NUMBER_KRYO4 = 0x4A953A82;

    /**
     * Kryo 5.x 版本的标识号，自版本 2.7 开始支持。
     *
     * @since 2.7
     */
    int IDENTITY_NUMBER_KRYO5 = 0xF6E0A5C0;

    /**
     * Fastjson 2.x 版本的标识号，自版本 2.7 开始支持。
     *
     * @since 2.7
     */
    @Deprecated
    int IDENTITY_NUMBER_FASTJSON2 = 0xF6E0A5C1;

}
