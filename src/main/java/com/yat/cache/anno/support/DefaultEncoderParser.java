package com.yat.cache.anno.support;

import com.yat.cache.anno.api.SerialPolicy;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.lang.Assert;
import com.yat.cache.core.support.encoders.GsonValueDecoder;
import com.yat.cache.core.support.encoders.GsonValueEncoder;
import com.yat.cache.core.support.encoders.JavaValueDecoder;
import com.yat.cache.core.support.encoders.JavaValueEncoder;
import com.yat.cache.core.support.encoders.Kryo5ValueDecoder;
import com.yat.cache.core.support.encoders.Kryo5ValueEncoder;
import com.yat.cache.core.support.encoders.KryoValueDecoder;
import com.yat.cache.core.support.encoders.KryoValueEncoder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * ClassName DefaultEncoderParser
 * <p>Description 默认编码器解析器</p>
 *
 * @author Yat
 * Date 2024/8/22 22:04
 * version 1.0
 */
public class DefaultEncoderParser implements EncoderParser {

    @Override
    public Function<Object, byte[]> parseEncoder(String valueEncoder) {
        Assert.notNull(valueEncoder, () -> new CacheConfigException("no serialPolicy"));

        valueEncoder = valueEncoder.trim();
        URI uri = URI.create(valueEncoder);
        valueEncoder = uri.getPath();
        boolean useIdentityNumber = isUseIdentityNumber(uri);
        if (SerialPolicy.KRYO.equalsIgnoreCase(valueEncoder)) {
            return new KryoValueEncoder(useIdentityNumber);
        } else if (SerialPolicy.JAVA.equalsIgnoreCase(valueEncoder)) {
            return new JavaValueEncoder(useIdentityNumber);
        } else if (SerialPolicy.KRYO5.equalsIgnoreCase(valueEncoder)) {
            return new Kryo5ValueEncoder(useIdentityNumber);
        } else if (SerialPolicy.GSON.equalsIgnoreCase(valueEncoder)) {
            return new GsonValueEncoder(useIdentityNumber);
        } else {
            throw new CacheConfigException("not supported:" + valueEncoder);
        }
    }

    @Override
    public Function<byte[], Object> parseDecoder(String valueDecoder) {
        Assert.notNull(valueDecoder, () -> new CacheConfigException("no serialPolicy"));

        valueDecoder = valueDecoder.trim();
        URI uri = URI.create(valueDecoder);
        valueDecoder = uri.getPath();
        boolean useIdentityNumber = isUseIdentityNumber(uri);
        if (SerialPolicy.KRYO.equalsIgnoreCase(valueDecoder)) {
            return new KryoValueDecoder(useIdentityNumber);
        } else if (SerialPolicy.JAVA.equalsIgnoreCase(valueDecoder)) {
            return javaValueDecoder(useIdentityNumber);
        } else if (SerialPolicy.KRYO5.equalsIgnoreCase(valueDecoder)) {
            return new Kryo5ValueDecoder(useIdentityNumber);
        } else if (SerialPolicy.GSON.equalsIgnoreCase(valueDecoder)) {
            return new GsonValueDecoder(useIdentityNumber);
        } else {
            throw new CacheConfigException("not supported:" + valueDecoder);
        }
    }

    /**
     * 根据是否使用身份标识符创建 JavaValueDecoder 实例
     * 此方法提供了一个工厂功能，根据传入的参数来决定是否在解码时使用身份标识符
     * 身份标识符（useIdentityNumber）为 true，则在解码时使用身份标识符，否则不使用
     *
     * @param useIdentityNumber 指定解码时是否使用身份标识符的布尔值
     * @return 返回一个根据参数初始化的 JavaValueDecoder 实例
     */
    JavaValueDecoder javaValueDecoder(boolean useIdentityNumber) {
        return new JavaValueDecoder(useIdentityNumber);
    }

    private boolean isUseIdentityNumber(URI uri) {
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        Boolean useIdentityNumber = Boolean.TRUE;
        if ("false".equalsIgnoreCase(params.get("useIdentityNumber"))) {
            useIdentityNumber = Boolean.FALSE;
        }
        return useIdentityNumber;
    }

    /**
     * 解析查询参数字符串为键值对映射
     * 该方法接收一个查询字符串，将其解析为一个Map，
     * 其中查询字符串中的每一部分都由'&'分隔，每一部分的键和值由'='分隔
     *
     * @param query 查询字符串，格式如 "key1=value1&key2=value2"
     * @return 返回一个键值对的Map，键是参数的名称，值是参数的值
     */
    protected static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> m = new HashMap<>();
        // 增加空值检查
        if (Objects.isNull(query) || query.isEmpty()) {
            return m;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? pair.substring(0, idx) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;

            if (value != null) {
                m.put(key, value);
            }
        }
        return m;
    }

}
