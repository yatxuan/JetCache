package com.yat.cache.core.support;

import com.yat.cache.anno.api.SerialPolicy;
import com.yat.cache.core.support.encoders.AbstractValueDecoder;
import com.yat.cache.core.support.encoders.GsonValueDecoder;
import com.yat.cache.core.support.encoders.JavaValueDecoder;
import com.yat.cache.core.support.encoders.Kryo5ValueDecoder;
import com.yat.cache.core.support.encoders.KryoValueDecoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huangli
 */
public class DecoderMap {

    private static final DecoderMap instance = new DecoderMap();
    private final ConcurrentHashMap<Integer, AbstractValueDecoder> decoderMap = new ConcurrentHashMap<>();
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private volatile boolean inited = false;

    public DecoderMap() {
    }

    public AbstractValueDecoder getDecoder(int identityNumber) {
        return decoderMap.get(identityNumber);
    }

    public void clear() {
        decoderMap.clear();
    }

    public ReentrantLock getLock() {
        return reentrantLock;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    public void initDefaultDecoder() {
        if (inited) {
            return;
        }
        reentrantLock.lock();
        try {
            if (inited) {
                return;
            }
            register(SerialPolicy.IDENTITY_NUMBER_JAVA, defaultJavaValueDecoder());
            register(SerialPolicy.IDENTITY_NUMBER_KRYO4, KryoValueDecoder.INSTANCE);
            register(SerialPolicy.IDENTITY_NUMBER_KRYO5, Kryo5ValueDecoder.INSTANCE);
            register(SerialPolicy.IDENTITY_NUMBER_GSON, GsonValueDecoder.INSTANCE);
            inited = true;
        } finally {
            reentrantLock.unlock();
        }
    }

    public void register(int identityNumber, AbstractValueDecoder decoder) {
        decoderMap.put(identityNumber, decoder);
    }

    public static JavaValueDecoder defaultJavaValueDecoder() {
        try {
            Class.forName("org.springframework.core.ConfigurableObjectInputStream");
            return SpringJavaValueDecoder.INSTANCE;
        } catch (ClassNotFoundException e) {
            return JavaValueDecoder.INSTANCE;
        }
    }

    public static DecoderMap defaultInstance() {
        return instance;
    }


}
