package com.yat.cache.core.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.function.Function;

/**
 * ClassName GsonKeyConvertor
 * Description TODO
 *
 * @author Yat
 * Date 2024/9/20 17:22
 * version 1.0
 */
public class GsonKeyConvertor implements Function<Object, Object> {

    public static final GsonKeyConvertor INSTANCE = new GsonKeyConvertor();
    private final Gson gson = new GsonBuilder().create();


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
