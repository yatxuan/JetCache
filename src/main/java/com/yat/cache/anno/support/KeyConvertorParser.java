/**
 * Created on 2019/6/7.
 */
package com.yat.cache.anno.support;

import java.util.function.Function;

/**
 * @author huangli
 */
public interface KeyConvertorParser {
    Function<Object, Object> parseKeyConvertor(String convertor);
}
