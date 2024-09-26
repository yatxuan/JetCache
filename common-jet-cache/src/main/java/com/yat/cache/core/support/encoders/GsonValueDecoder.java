package com.yat.cache.core.support.encoders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;

/**
 * ClassName GsonValueDecoder
 * Description 基于 Gson 的值解码器
 *
 * @author Yat
 * Date 2024/8/25 下午2:02
 * version 1.0
 */
public class GsonValueDecoder extends AbstractJsonDecoder {

    public static final GsonValueDecoder INSTANCE = new GsonValueDecoder(true);
    private final Gson gson = new GsonBuilder().create();

    public GsonValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 将字节数组解析成指定类型的对象。
     * 该方法重写了AbstractJsonDecoder中的方法，使用 Gson 进行JSON解析。
     *
     * @param buffer 字节数组，包含待解析的JSON数据。
     * @param index  字节数组中JSON数据的起始位置。
     * @param len    要解析的JSON数据的长度。
     * @param clazz  目标对象的类，解析后的对象将属于这个类。
     * @return 解析后的对象。
     */
    @Override
    protected Object parseObject(byte[] buffer, int index, int len, Class<?> clazz) {
        // 将字节数组转换为字符串，使用UTF-8 编码。
        String s = new String(buffer, index, len, StandardCharsets.UTF_8);
        // 使用 Gson 将字符串解析为指定类型的对象。
        return gson.fromJson(s, clazz);
    }
}
