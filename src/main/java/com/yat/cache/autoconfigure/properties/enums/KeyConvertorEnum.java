package com.yat.cache.autoconfigure.properties.enums;

/**
 * ClassName KeyConvertorEnum
 * Description key转换器的全局配置，仅当使用@CreateCache且缓存类型为LOCAL时可以指定为none，此时通过equals方法来识别key。
 * 方法缓存必须指定KeyConvertor
 *
 * @author Yat
 * Date 2024/8/23 12:11
 * version 1.0
 */
public enum KeyConvertorEnum {
    NONE,
    GSON,
    JACKSON,
}
