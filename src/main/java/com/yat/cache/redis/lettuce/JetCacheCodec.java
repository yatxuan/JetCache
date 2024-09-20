package com.yat.cache.redis.lettuce;


import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * Created on 2017/4/28.
 *
 * @author huangli
 */
public class JetCacheCodec implements RedisCodec {

    @Override
    public Object decodeKey(ByteBuffer bytes) {
        return convert(bytes);
    }

    @Override
    public Object decodeValue(ByteBuffer bytes) {
        return convert(bytes);
    }

    @Override
    public ByteBuffer encodeKey(Object key) {
        byte[] bytes = (byte[]) key;
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public ByteBuffer encodeValue(Object value) {
        byte[] bytes = (byte[]) value;
        return ByteBuffer.wrap(bytes);
    }

    private Object convert(ByteBuffer bytes){
        byte[] bs = new byte[bytes.remaining()];
        bytes.get(bs);
        return bs;
    }


}
