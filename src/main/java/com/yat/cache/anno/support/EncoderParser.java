package com.yat.cache.anno.support;

import java.util.function.Function;

/**
 * ClassName EncoderParser
 * <p>Description 编码解析器接口，用于定义如何序列化和反序列化对象</p>
 * <p>该接口提供两个方法，分别用于获取序列化和反序列化的函数</p>
 *
 * @author Yat
 * Date 2024/8/22 22:06
 * version 1.0
 */
public interface EncoderParser {

    /**
     * 获取用于序列化对象的函数
     *
     * @param valueEncoder 序列化策略类型
     * @return 将对象序列化为字节数组的函数
     */
    Function<Object, byte[]> parseEncoder(String valueEncoder);

    /**
     * 获取用于反序列化对象的函数
     *
     * @param valueDecoder 反序列化策略类型
     * @return 将字节数组反序列化为对象的函数
     */
    Function<byte[], Object> parseDecoder(String valueDecoder);
}
