package com.yat.cache.core.support.encoders;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.CompatibleFieldSerializer;
import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
import com.yat.cache.core.exception.CacheEncodeException;
import com.yat.cache.core.support.ObjectPool;
import lombok.Getter;

/**
 * ClassName Kryo5ValueEncoder
 * <p>Description 基于Kryo5的值编码器，用于序列化对象</p>
 *
 * @author Yat
 * Date 2024/8/22 19:48
 * version 1.0
 */
public class Kryo5ValueEncoder extends AbstractValueEncoder {

    /**
     * Kryo5缓存对象池，用于复用Kryo5和Output实例
     * 默认大小为32K
     */
    static ObjectPool<Kryo5Cache> kryoCacheObjectPool = new ObjectPool<>(16,
            new ObjectPool.ObjectFactory<>() {
                @Override
                public Kryo5Cache create() {
                    return new Kryo5Cache();
                }

                @Override
                public void reset(Kryo5Cache obj) {
                    obj.getKryo().reset();
                    obj.getOutput().reset();
                }
            });
    /**
     * 初始缓冲区大小
     */
    private static final int INIT_BUFFER_SIZE = 2048;
    /**
     * 单例实例，支持身份编码的Kryo5ValueEncoder
     */
    public static final Kryo5ValueEncoder INSTANCE = new Kryo5ValueEncoder(true);

    public Kryo5ValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 序列化给定的对象值。
     *
     * @param value 要序列化的对象
     * @return 序列化后的字节数组
     * @throws CacheEncodeException 如果序列化过程中发生错误
     */
    @Override
    public byte[] apply(Object value) {
        Kryo5Cache kryoCache = null;
        try {
            kryoCache = kryoCacheObjectPool.borrowObject();
            if (useIdentityNumber) {
                writeInt(kryoCache.getOutput());
            }
            kryoCache.getKryo().writeClassAndObject(kryoCache.getOutput(), value);
            return kryoCache.getOutput().toBytes();
        } catch (Exception e) {
            throw new CacheEncodeException("Kryo Encode error. " + "msg=" + e.getMessage(), e);
        } finally {
            if (kryoCache != null) {
                kryoCacheObjectPool.returnObject(kryoCache);
            }
        }
    }

    /**
     * 手动写入一个整数到Output中。
     * Kryo5改变了写入整数的方式，使用小端字节序，
     * 因此在这里手动实现整数的写入。
     *
     * @param output 要写入的Output流
     */
    private void writeInt(Output output) {
        // kryo5 change writeInt to little endian, so we write int manually
        int identityNumberKryo5 = SerialPolicyTypeEnum.KRYO5.getCode();
        output.writeByte(identityNumberKryo5 >>> 24);
        output.writeByte(identityNumberKryo5 >>> 16);
        output.writeByte(identityNumberKryo5 >>> 8);
        output.writeByte(identityNumberKryo5);
    }

    /**
     * Kryo5缓存类，封装了Kryo实例和Output流，
     * 用于Kryo5序列化操作。
     */
    @Getter
    public static class Kryo5Cache {
        /**
         * 获取Output流
         */
        final Output output;
        /**
         * 获取Kryo实例
         */
        final Kryo kryo;

        /**
         * 初始化Kryo实例和Output流。
         */
        public Kryo5Cache() {
            kryo = new Kryo();
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            kryo.setRegistrationRequired(false);
            output = new Output(INIT_BUFFER_SIZE, -1);
        }

    }

}
