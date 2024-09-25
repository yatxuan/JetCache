package com.yat.cache.core.support.encoders;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
import com.yat.cache.core.exception.CacheEncodeException;
import com.yat.cache.core.support.ObjectPool;
import lombok.Getter;

/**
 * ClassName KryoValueEncoder
 * <p>Description 利用 Kryo 库进行高效的对象序列化</p>
 * * Kryo 是一个快速、有效的对象图序列化库，适用于 Java。
 * * 本类通过静态实例和对象池技术管理 Kryo 资源，以提高性能和资源复用。
 *
 * @author Yat
 * Date 2024/8/22 19:54
 * version 1.0
 */
public class KryoValueEncoder extends AbstractValueEncoder {
    /**
     * KryoCache 对象池，用于管理 Kryo 资源的复用
     * Default size = 32K
     */
    static ObjectPool<KryoCache> kryoCacheObjectPool = new ObjectPool<>(
            16, new ObjectPool.ObjectFactory<>() {
        @Override
        public KryoCache create() {
            return new KryoCache();
        }

        @Override
        public void reset(KryoCache obj) {
            obj.getKryo().reset();
            obj.getOutput().clear();
        }
    }
    );
    /**
     * 初始化缓冲区大小
     */
    private static final int INIT_BUFFER_SIZE = 2048;
    /**
     * KryoValueEncoder 的单例实例
     */
    public static final KryoValueEncoder INSTANCE = new KryoValueEncoder(true);

    /**
     * 初始化 KryoValueEncoder。
     *
     * @param useIdentityNumber 是否使用对象的唯一标识号。
     */
    public KryoValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 序列化对象为字节数组。
     *
     * @param value 要序列化的对象。
     * @return 序列化后的字节数组。
     * @throws CacheEncodeException 如果序列化过程中发生错误。
     */
    @Override
    public byte[] apply(Object value) {
        KryoCache kryoCache = null;
        try {
            kryoCache = kryoCacheObjectPool.borrowObject();
            Output output = kryoCache.getOutput();
            if (useIdentityNumber) {
                writeInt(output);
            }
            kryoCache.getKryo().writeClassAndObject(output, value);
            return output.toBytes();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        } finally {
            if (kryoCache != null) {
                kryoCacheObjectPool.returnObject(kryoCache);
            }
        }
    }

    /**
     * 手动写入 int 值到输出流。
     *
     * @param output 输出流。
     */
    private void writeInt(Output output) {
        // kryo5 change writeInt to little endian, so we write int manually
        int identityNumberKryo4 = SerialPolicyTypeEnum.KRYO.getCode();
        output.writeByte(identityNumberKryo4 >>> 24);
        output.writeByte(identityNumberKryo4 >>> 16);
        output.writeByte(identityNumberKryo4 >>> 8);
        output.writeByte(identityNumberKryo4);
    }

    /**
     * KryoCache 内部类，封装了 Kryo 和 Output，用于高效序列化。
     */
    @Getter
    public static class KryoCache {
        final Output output;
        final Kryo kryo;

        public KryoCache() {
            kryo = new Kryo();
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            byte[] buffer = new byte[INIT_BUFFER_SIZE];
            output = new Output(buffer, -1);
        }

    }

}
