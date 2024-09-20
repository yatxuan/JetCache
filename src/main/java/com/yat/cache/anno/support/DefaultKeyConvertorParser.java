package com.yat.cache.anno.support;

import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.core.CacheConfigException;
import com.yat.cache.core.support.Fastjson2KeyConvertor;
import com.yat.cache.core.support.GsonKeyConvertor;
import com.yat.cache.core.support.JacksonKeyConvertor;

import java.util.function.Function;

/**
 * @author huangli
 */
public class DefaultKeyConvertorParser implements KeyConvertorParser {
    @Override
    public Function<Object, Object> parseKeyConvertor(String convertor) {
        if (convertor == null) {
            return null;
        }
        if (KeyConvertor.GSON.equalsIgnoreCase(convertor)) {
            return GsonKeyConvertor.INSTANCE;
        } else if (KeyConvertor.FASTJSON2.equalsIgnoreCase(convertor)) {
            return Fastjson2KeyConvertor.INSTANCE;
        } else if (KeyConvertor.JACKSON.equalsIgnoreCase(convertor)) {
            return JacksonKeyConvertor.INSTANCE;
        } else if (KeyConvertor.NONE.equalsIgnoreCase(convertor)) {
            return KeyConvertor.NONE_INSTANCE;
        }
        throw new CacheConfigException("not supported:" + convertor);
    }
}
