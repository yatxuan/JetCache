package com.yat.cache.core.support.encoders;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;

/**
 * ClassName GsonValueEncoder
 * Description Gson 值编码器:使用 Gson 将 Java 对象转换为字节数组的功能
 *
 * @author Yat
 * Date 2024/8/25 下午1:37
 * version 1.0
 */
public class GsonValueEncoder extends AbstractJsonEncoder {

    public static final GsonValueEncoder INSTANCE = new GsonValueEncoder(true);
    private final Gson gson = new GsonBuilder().create();

    public GsonValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 应用序列化逻辑。
     *
     * @param value 要序列化的对象。
     * @return 序列化后的字节数组。
     */
    @Override
    protected byte[] encodeSingleValue(Object value) {
        return gson.toJson(value).getBytes(StandardCharsets.UTF_8);
    }

}
