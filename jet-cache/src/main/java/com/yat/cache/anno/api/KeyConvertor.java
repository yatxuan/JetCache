package com.yat.cache.anno.api;

import java.util.function.Function;

/**
 * ClassName KeyConvertor
 * <p>Description 自定义键转换器只需实现 Function<Object, Object> 即可。</p>
 * <p>如果键转换器实现了这个接口，它可以处理 byte[] 和 String 类型的数据，具体见 AbstractExternalCache 类。</p>
 *
 * @author Yat
 * Date 2024/8/22 09:55
 * version 1.0
 */
public interface KeyConvertor extends Function<Object, Object> {

    /**
     * 提供了一个预定义的 Function 实例，表示不进行任何转换。
     */
    Function<Object, Object> NONE_INSTANCE = k -> k;
}
