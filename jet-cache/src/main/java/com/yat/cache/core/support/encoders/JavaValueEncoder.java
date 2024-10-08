package com.yat.cache.core.support.encoders;

import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
import com.yat.cache.core.exception.CacheEncodeException;
import com.yat.cache.core.support.ObjectPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * ClassName JavaValueEncoder
 * <p>Description Java 值编码器</p>
 * <p>
 * 此类实现了将 Java 对象序列化为字节数组的功能。
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 13:07
 * version 1.0
 */
public class JavaValueEncoder extends AbstractValueEncoder {


    /**
     * 初始化缓冲区大小。
     */
    private static final int INIT_BUF_SIZE = 2048;
    /**
     * 字节数组输出流对象池。
     */
    static ObjectPool<ByteArrayOutputStream> bosPool = new ObjectPool<>(16,
            new ObjectPool.ObjectFactory<>() {
                @Override
                public ByteArrayOutputStream create() {
                    return new ByteArrayOutputStream(INIT_BUF_SIZE);
                }

                @Override
                public void reset(ByteArrayOutputStream obj) {
                    obj.reset();
                }
            }
    );
    public static final JavaValueEncoder INSTANCE = new JavaValueEncoder(true);

    public JavaValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        ByteArrayOutputStream bos = null;
        try {
            bos = bosPool.borrowObject();
            if (useIdentityNumber) {
                int identityNumberJava = SerialPolicyTypeEnum.JAVA.getCode();
                bos.write((identityNumberJava >> 24) & 0xFF);
                bos.write((identityNumberJava >> 16) & 0xFF);
                bos.write((identityNumberJava >> 8) & 0xFF);
                bos.write(identityNumberJava & 0xFF);
            }
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new CacheEncodeException("Java Encode error. msg=" + e.getMessage(), e);
        } finally {
            if (bos != null) {
                bosPool.returnObject(bos);
            }
        }
    }
}
