package com.yat.cache.anno.support;

import java.util.function.Function;

/**
 * ClassName KeyConvertorParser
 * <p>Description 关键字转换器解析器接口，用于根据配置的转换器枚举解析出具体的转换函数</p>
 *
 * @author Yat
 * Date 2024/8/22 22:06
 * version 1.0
 */
public interface KeyConvertorParser {

    /**
     * 根据关键字转换器枚举解析出对应的转换函数
     * <p>Description 该方法用于将枚举类型的转换器配置转换为可执行的转换函数，用于后续的数据转换操作</p>
     *
     * @param convertor 关键字转换器枚举，指示需要使用的转换器类型
     * @return Function<Object, Object> 返回一个函数，用于执行实际的转换操作
     */
    Function<Object, Object> parseKeyConvertor(String convertor);
}
