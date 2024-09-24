package com.yat.cache.autoconfigure.properties.enums;

/**
 * ClassName SerialPolicyTypeEnum
 * Description 序列化策略
 *
 * @author Yat
 * Date 2024/8/23 12:05
 * version 1.0
 */
public enum SerialPolicyTypeEnum {

    JAVA,
    KRYO,
    KRYO5,
    GSON,
    /**
     * 自定义序列化解码器
     */
    BEAN_DECODER_CUSTOM,
    /**
     * 自定义序列化编码器
     */
    BEAN_ENCODER_CUSTOM,
}
