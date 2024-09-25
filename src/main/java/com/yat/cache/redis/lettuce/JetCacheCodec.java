package com.yat.cache.redis.lettuce;

import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * ClassName JetCacheCodec
 * <p>Description 自定义的Redis编码解码类JetCacheCodec，实现了RedisCodec接口
 * 主要用于在Redis操作中，对键和值的字节编码和解码。</p>
 *
 * @author Yat
 * Date 2024/9/25 09:23
 * version 1.0
 */
public class JetCacheCodec implements RedisCodec {

    /**
     * 解码Redis键。
     *
     * @param bytes 字节缓冲区
     * @return 解码后的键对象
     */
    @Override
    public Object decodeKey(ByteBuffer bytes) {
        return convert(bytes);
    }

    /**
     * 解码Redis值。
     *
     * @param bytes 字节缓冲区
     * @return 解码后的值对象
     */
    @Override
    public Object decodeValue(ByteBuffer bytes) {
        return convert(bytes);
    }

    /**
     * 编码Redis键。
     *
     * @param key 要编码的键
     * @return 编码后的键的字节缓冲区
     */
    @Override
    public ByteBuffer encodeKey(Object key) {
        byte[] bytes = (byte[]) key;
        return ByteBuffer.wrap(bytes);
    }

    /**
     * 编码Redis值。
     *
     * @param value 要编码的值
     * @return 编码后的值的字节缓冲区
     */
    @Override
    public ByteBuffer encodeValue(Object value) {
        byte[] bytes = (byte[]) value;
        return ByteBuffer.wrap(bytes);
    }

    /**
     * 将ByteBuffer转换为byte数组。
     *
     * @param bytes ByteBuffer对象
     * @return 转换后的byte数组
     */
    private Object convert(ByteBuffer bytes) {
        byte[] bs = new byte[bytes.remaining()];
        bytes.get(bs);
        return bs;
    }
}
