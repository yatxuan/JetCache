package com.yat.cache.anno.api;

import java.util.function.Function;

/**
 * ClassName SerialPolicy
 * <p>Description 序列化策略接口定义了不同的序列化方式以及相应的编码器和解码器</p>
 *
 * @author Yat
 * Date 2024/8/22 09:58
 * version 1.0
 */
public interface SerialPolicy extends Identity {

    String JAVA = "JAVA";

    String GSON = "GSON";

    String KRYO = "KRYO";

    String KRYO5 = "KRYO5";

    /**
     * 获取编码器，用于将对象转换为字节数组。
     *
     * @return 编码器函数
     */
    Function<Object, byte[]> encoder();

    /**
     * 获取解码器，用于将字节数组转换为对象。
     *
     * @return 解码器函数
     */
    Function<byte[], Object> decoder();
}
