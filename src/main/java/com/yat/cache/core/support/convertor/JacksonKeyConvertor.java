package com.yat.cache.core.support.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yat.cache.core.exception.CacheEncodeException;

import java.util.function.Function;

/**
 * ClassName JacksonKeyConvertor
 * <p>Description 使用Jackson进行键转换的工具类</p>
 *
 * @author Yat
 * Date 2024/8/22 18:07
 * version 1.0
 */
public class JacksonKeyConvertor implements Function<Object, Object> {

    /**
     * 共享的ObjectMapper实例，用于对象的序列化和反序列化
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 单例实例，供外部使用
     */
    public static final JacksonKeyConvertor INSTANCE = new JacksonKeyConvertor();

    /**
     * 将给定的原始键对象转换为适合作为缓存键的字符串。
     * 如果原始键为null，则返回null。
     * 如果原始键是CharSequence的实例，则将其转换为字符串并返回。
     * 否则，使用ObjectMapper将原始键对象序列化为JSON字符串。
     *
     * @param originalKey 原始的键对象
     * @return 转换后的缓存键字符串
     * @throws CacheEncodeException 如果序列化过程中发生JsonProcessingException
     */
    @Override
    public Object apply(Object originalKey) {
        if (originalKey == null) {
            return null;
        }
        if (originalKey instanceof CharSequence) {
            return originalKey.toString();
        }
        try {
            return objectMapper.writeValueAsString(originalKey);
        } catch (JsonProcessingException e) {
            throw new CacheEncodeException("jackson key convert fail", e);
        }
    }

}

