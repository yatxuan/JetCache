package com.yat.cache.core.support.encoders;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;

import java.io.ByteArrayInputStream;

/**
 * ClassName Kryo5ValueDecoder
 * <p>Description Kryo5ValueDecoder类是用于解码Kryo5序列化数据的工具类</p>
 * Kryo5是一种高效的对象图序列化框架，常用于对象的序列化与反序列化操作。
 * 该类主要通过Kryo5实现数据的反序列化操作，即从字节数组中还原出原始对象。
 *
 * @author Yat
 * Date 2024/8/22 19:43
 * version 1.0
 */
public class Kryo5ValueDecoder extends AbstractValueDecoder {

    /**
     * 单例实例，用于减少对象创建开销
     */
    public static final Kryo5ValueDecoder INSTANCE = new Kryo5ValueDecoder(true);

    /**
     * 接受一个决定是否使用身份编号的参数
     */
    public Kryo5ValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 执行实际的反序列化操作。
     * 该方法首先根据是否使用身份编号决定输入流的创建方式，然后利用Kryo5从字节数组中读取并还原对象。
     *
     * @param buffer 包含序列化对象数据的字节数组
     * @return 反序列化后的原始对象
     */
    @Override
    public Object doApply(byte[] buffer) {
        ByteArrayInputStream in;
        // 根据是否使用身份编号，决定跳过字节数组中的前4个字节
        if (useIdentityNumber) {
            in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
        } else {
            in = new ByteArrayInputStream(buffer);
        }
        Input input = new Input(in);
        Kryo5ValueEncoder.Kryo5Cache kryoCache = null;
        try {
            // 从对象池中借用Kryo5缓存对象，提高性能
            kryoCache = Kryo5ValueEncoder.kryoCacheObjectPool.borrowObject();
            Kryo kryo = kryoCache.getKryo();
            // 获取当前线程的上下文类加载器，用于Kryo的类加载
            ClassLoader classLoader = Kryo5ValueDecoder.class.getClassLoader();
            ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
            if (ctxClassLoader != null) {
                classLoader = ctxClassLoader;
            }
            kryo.setClassLoader(classLoader);
            // 使用Kryo读取并还原类和对象
            return kryo.readClassAndObject(input);
        } finally {
            // 归还Kryo5缓存对象到对象池
            if (kryoCache != null) {
                Kryo5ValueEncoder.kryoCacheObjectPool.returnObject(kryoCache);
            }
        }
    }
}
