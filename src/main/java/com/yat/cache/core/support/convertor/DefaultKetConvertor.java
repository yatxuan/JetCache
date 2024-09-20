package com.yat.cache.core.support.convertor;

import java.util.function.Function;

/**
 * ClassName DefaultKetConvertor
 * Description 默认的键转换器
 *
 * @author Yat
 * Date 2024/9/20 17:41
 * version 1.0
 */
public interface DefaultKetConvertor {

    /**
     * 提供了一个预定义的 Function 实例，表示不进行任何转换。
     */
    Function<Object, Object> NONE_INSTANCE = k -> k;
}
