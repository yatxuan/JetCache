package com.yat.cache.anno.support;

import java.util.function.Function;

/**
 * ClassName ParserFunction
 * <p>Description KeyConvertorEnum转换器函数</p>
 *
 * @author Yat
 * Date 2024/9/23 15:46
 * version 1.0
 */
public record ParserFunction(String value) implements Function<Object, Object> {

    @Override
    public Object apply(Object t) {
        throw new UnsupportedOperationException();
    }
}
