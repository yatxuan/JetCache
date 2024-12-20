package com.yat.cache.anno.support;

import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.autoconfigure.properties.enums.KeyConvertorEnum;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.support.convertor.GsonKeyConvertor;
import com.yat.cache.core.support.convertor.JacksonKeyConvertor;

import java.util.Objects;
import java.util.function.Function;

/**
 * ClassName DefaultKeyConvertorParser
 * <p>Description 默认的KeyConvertor解析器</p>
 *
 * @author Yat
 * Date 2024/8/22 22:05
 * version 1.0
 */
public class DefaultKeyConvertorParser implements KeyConvertorParser {

    @Override
    public Function<Object, Object> parseKeyConvertor(String convertor) {
        if (Objects.isNull(convertor)) {
            return null;
        }
        if (KeyConvertorEnum.GSON.name().equalsIgnoreCase(convertor)) {
            return GsonKeyConvertor.INSTANCE;
        } else if (KeyConvertorEnum.JACKSON.name().equalsIgnoreCase(convertor)) {
            return JacksonKeyConvertor.INSTANCE;
        } else if (KeyConvertorEnum.NONE.name().equalsIgnoreCase(convertor)) {
            return KeyConvertor.NONE_INSTANCE;
        }
        throw new CacheConfigException("not supported:" + convertor);
    }
}
