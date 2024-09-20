package com.yat.cache.core.support.convertor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.function.Function;

/**
 * ClassName GsonKeyConvertor
 * Description 使用 Gson 实现的键转换器
 *
 * @author Yat
 * Date 2024/8/24 下午11:50
 * version 1.0
 */
public class GsonKeyConvertor implements Function<Object, Object> {
    /**
     * 单例实例，因为键转换器不需要每次创建新的实例
     */
    public static final GsonKeyConvertor INSTANCE = new GsonKeyConvertor();
    private final Gson gson = new GsonBuilder().create();

    /**
     * 将给定对象转换为字符串，适合作为缓存键
     * 如果对象为null，则返回null。如果对象已经是String类型，则直接返回
     * 否则，使用Gson将其转换为JSON格式的字符串
     *
     * @param originalKey 原始键对象，可以是任意类型
     * @return 转换后的字符串键，或null
     */
    @Override
    public Object apply(Object originalKey) {
        if (originalKey == null) {
            return null;
        }
        if (originalKey instanceof String) {
            return originalKey;
        }
        return gson.toJson(originalKey);
    }
}
