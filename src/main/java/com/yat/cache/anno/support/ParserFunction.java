package com.yat.cache.anno.support;

import com.yat.cache.anno.api.KeyConvertor;

/**
 * ClassName ParserFunction
 * <p>Description KeyConvertorEnum转换器函数</p>
 *
 * @author Yat
 * Date 2024/9/23 15:46
 * version 1.0
 */
public record ParserFunction(String value) implements KeyConvertor {

    @Override
    public Object apply(Object t) {
        throw new UnsupportedOperationException();
    }
}
