package com.yat.cache.autoconfigure.properties.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName SerialPolicyTypeEnum
 * Description 序列化策略
 *
 * @author Yat
 * Date 2024/8/23 12:05
 * version 1.0
 */
@Getter
@AllArgsConstructor
public enum SerialPolicyTypeEnum {

    JAVA(0x4A953A80),
    KRYO(0x4A953A82),
    KRYO5(0xF6E0A5C0),
    GSON(0xF6E0A5C1),
    ;
    /**
     * 序列化的标识号
     */
    private final int code;
}
