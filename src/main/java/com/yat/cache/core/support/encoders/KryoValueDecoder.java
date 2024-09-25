package com.yat.cache.core.support.encoders;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import java.io.ByteArrayInputStream;

/**
 * ClassName KryoValueDecoder
 * <p>Description 使用Kryo库进行对象的反序列化</p>
 *
 * @author Yat
 * Date 2024/8/22 19:53
 * version 1.0
 */
public class KryoValueDecoder extends AbstractValueDecoder {

    public static final KryoValueDecoder INSTANCE = new KryoValueDecoder(true);

    /**
     * 初始化是否使用标识号进行解码
     *
     * @param useIdentityNumber 标志是否使用标识号进行解码
     */
    public KryoValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 实际执行反序列化的函数从字节数组中读取对象信息，并通过Kryo库还原对象
     *
     * @param buffer 包含序列化对象数据的字节数组
     * @return 反序列化后的原始对象
     */
    @Override
    public Object doApply(byte[] buffer) {
        ByteArrayInputStream in;
        if (useIdentityNumber) {
            in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
        } else {
            in = new ByteArrayInputStream(buffer);
        }
        Input input = new Input(in);
        KryoValueEncoder.KryoCache kryoCache = null;
        try {
            kryoCache = KryoValueEncoder.kryoCacheObjectPool.borrowObject();
            Kryo kryo = kryoCache.getKryo();
            // 获取当前线程的上下文类加载器，用于处理类加载问题
            ClassLoader classLoader = KryoValueDecoder.class.getClassLoader();
            Thread t = Thread.currentThread();
            if (t != null) {
                ClassLoader ctxClassLoader = t.getContextClassLoader();
                if (ctxClassLoader != null) {
                    classLoader = ctxClassLoader;
                }
            }
            kryo.setClassLoader(classLoader);
            return kryo.readClassAndObject(input);
        } finally {
            if (kryoCache != null) {
                KryoValueEncoder.kryoCacheObjectPool.returnObject(kryoCache);
            }
        }
    }
}
