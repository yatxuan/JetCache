package com.yat.cache.core.support;

import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
import com.yat.cache.core.support.encoders.AbstractValueDecoder;
import com.yat.cache.core.support.encoders.GsonValueDecoder;
import com.yat.cache.core.support.encoders.JavaValueDecoder;
import com.yat.cache.core.support.encoders.Kryo5ValueDecoder;
import com.yat.cache.core.support.encoders.KryoValueDecoder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName DecoderMap
 * <p>Description Decoder映射类，用于管理和提供不同的Decoder实例</p>
 *
 * @author Yat
 * Date 2024/8/22 17:56
 * version 1.0
 */
@NoArgsConstructor
public class DecoderMap {

    /**
     * 单例实例。
     */
    private static final DecoderMap instance = new DecoderMap();
    /**
     * 用于存储和管理Decoder的容器。
     */
    private final ConcurrentHashMap<Integer, AbstractValueDecoder> decoderMap = new ConcurrentHashMap<>();
    /**
     * 用于同步初始化操作的锁。
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();
    /**
     * 标记DecoderMap是否已经被初始化的标志位
     */
    @Setter
    private volatile boolean inited = false;


    /**
     * 获取指定标识号的解码器。
     *
     * @param identityNumber 解码器的标识号
     * @return 对应的解码器实例，如果不存在则返回null
     */
    public AbstractValueDecoder getDecoder(int identityNumber) {
        return decoderMap.get(identityNumber);
    }

    /**
     * 清空解码器映射表。
     */
    public void clear() {
        decoderMap.clear();
    }

    /**
     * 获取用于线程同步的重入锁。
     *
     * @return 重入锁对象
     */
    public ReentrantLock getLock() {
        return reentrantLock;
    }

    /**
     * 初始化默认解码器
     * 该方法采用双重检查锁定模式，确保只初始化一次
     */
    public void initDefaultDecoder() {
        if (inited) {
            return;
        }
        reentrantLock.lock();
        try {
            if (inited) {
                return;
            }
            // 注册默认的Java值解码器
            register(SerialPolicyTypeEnum.JAVA.getCode(), defaultJavaValueDecoder());
            // 注册Kryo4的解码器
            register(SerialPolicyTypeEnum.KRYO.getCode(), KryoValueDecoder.INSTANCE);
            // 注册Kryo5的解码器
            register(SerialPolicyTypeEnum.KRYO5.getCode(), Kryo5ValueDecoder.INSTANCE);
            // 注册Gson的解码器
            register(SerialPolicyTypeEnum.GSON.getCode(), GsonValueDecoder.INSTANCE);
            inited = true;
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 注册新的解码器到映射表中。
     *
     * @param identityNumber 解码器的标识号
     * @param decoder        要注册的解码器
     */
    public void register(int identityNumber, AbstractValueDecoder decoder) {
        decoderMap.put(identityNumber, decoder);
    }

    /**
     * 获取默认的Java值解码器。
     *
     * @return 默认Java值解码器
     */
    public static JavaValueDecoder defaultJavaValueDecoder() {
        try {
            // 尝试加载Spring的类来判断是否使用Spring的Java值解码器
            Class.forName("org.springframework.core.ConfigurableObjectInputStream");
            return SpringJavaValueDecoder.INSTANCE;
        } catch (ClassNotFoundException e) {
            // 如果没有找到Spring相关的类，则使用默认的Java值解码器
            return JavaValueDecoder.INSTANCE;
        }
    }

    /**
     * 获取DecoderMap的默认实例。
     *
     * @return DecoderMap实例
     */
    public static DecoderMap defaultInstance() {
        return instance;
    }


}
