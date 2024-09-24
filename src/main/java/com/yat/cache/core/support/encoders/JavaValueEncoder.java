package com.yat.cache.core.support.encoders;

import com.yat.cache.anno.api.SerialPolicy;
import com.yat.cache.core.exception.CacheEncodeException;
import com.yat.cache.core.support.ObjectPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created on 2016/10/4.
 *
 * @author huangli
 */
public class JavaValueEncoder extends AbstractValueEncoder {

    private static final int INIT_BUF_SIZE = 2048;
    static ObjectPool<ByteArrayOutputStream> bosPool = new ObjectPool<>(16,
            new ObjectPool.ObjectFactory<ByteArrayOutputStream>() {
                @Override
                public ByteArrayOutputStream create() {
                    return new ByteArrayOutputStream(INIT_BUF_SIZE);
                }

                @Override
                public void reset(ByteArrayOutputStream obj) {
                    obj.reset();
                }
            });
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
                bos.write((SerialPolicy.IDENTITY_NUMBER_JAVA >> 24) & 0xFF);
                bos.write((SerialPolicy.IDENTITY_NUMBER_JAVA >> 16) & 0xFF);
                bos.write((SerialPolicy.IDENTITY_NUMBER_JAVA >> 8) & 0xFF);
                bos.write(SerialPolicy.IDENTITY_NUMBER_JAVA & 0xFF);
            }
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Java Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        } finally {
            if (bos != null) {
                bosPool.returnObject(bos);
            }
        }
    }
}
