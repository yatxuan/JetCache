package com.yat.cache.core.support.encoders;

import com.yat.cache.core.CacheValueHolder;
import com.yat.cache.core.support.CacheMessage;

import java.nio.charset.StandardCharsets;

/**
 * ClassName AbstractJsonDecoder
 * <p>Description 抽象 JSON 解码器类，用于从字节数组中解码出缓存值</p>
 *
 * @author Yat
 * Date 2024/8/22 17:40
 * version 1.0
 */
public abstract class AbstractJsonDecoder extends AbstractValueDecoder {

    /**
     * 初始化是否使用标识号。
     *
     * @param useIdentityNumber 是否使用标识号
     */
    public AbstractJsonDecoder(Boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 应用解码逻辑到给定的字节数组。
     *
     * @param buffer 待解码的字节数组
     * @return 解码后的对象
     * @throws Exception 如果解码过程中发生错误
     */
    @Override
    protected Object doApply(byte[] buffer) throws Exception {
        int[] indexHolder = new int[1];
        indexHolder[0] = isUseIdentityNumber() ? 4 : 0;
        short objCount = readShort(buffer, indexHolder[0]);
        indexHolder[0] = indexHolder[0] + 2;
        if (objCount < 0) {
            return null;
        }
        Object obj = readObject(buffer, indexHolder);
        if (obj == null) {
            return null;
        }
        if (obj instanceof CacheValueHolder h) {
            h.setValue(readObject(buffer, indexHolder));
            return h;
        } else if (obj instanceof CacheMessage cm) {
            if (objCount > 1) {
                Object[] keys = new Object[objCount - 1];
                for (int i = 0; i < objCount - 1; i++) {
                    keys[i] = readObject(buffer, indexHolder);
                }
                cm.setKeys(keys);
            }
            return cm;
        } else {
            return obj;
        }
    }

    /**
     * 从字节数组中读取短整型。
     *
     * @param buf   字节数组
     * @param index 开始读取的位置
     * @return 短整型值
     */
    private short readShort(byte[] buf, int index) {
        int x = buf[index] & 0xFF;
        x = (x << 8) | (buf[index + 1] & 0xFF);
        return (short) x;
    }

    /**
     * 从字节数组中读取对象。
     *
     * @param buf         字节数组
     * @param indexHolder 当前读取位置的索引持有者
     * @return 读取的对象
     * @throws Exception 如果读取过程中发生错误
     */
    private Object readObject(byte[] buf, int[] indexHolder) throws Exception {
        int index = indexHolder[0];
        short classNameLen = readShort(buf, index);
        index += 2;
        if (classNameLen < 0) {
            indexHolder[0] = index;
            return null;
        } else {
            String className = new String(buf, index, classNameLen, StandardCharsets.UTF_8);
            index += classNameLen;
            Class<?> clazz = Class.forName(className);
            int size = readInt(buf, index);
            index += 4;
            Object obj = parseObject(buf, index, size, clazz);
            index += size;
            indexHolder[0] = index;
            return obj;
        }
    }

    /**
     * 从字节数组中读取整数。
     *
     * @param buf   字节数组
     * @param index 开始读取的位置
     * @return 整数
     */
    private int readInt(byte[] buf, int index) {
        int x = buf[index] & 0xFF;
        x = (x << 8) | (buf[index + 1] & 0xFF);
        x = (x << 8) | (buf[index + 2] & 0xFF);
        x = (x << 8) | (buf[index + 3] & 0xFF);
        return x;
    }

    /**
     * 解析字节数组中的对象。
     *
     * @param buffer 字节数组
     * @param index  开始解析的位置
     * @param len    对象的长度
     * @param clazz  对象的类
     * @return 解析后的对象
     */
    protected abstract Object parseObject(byte[] buffer, int index, int len, Class<?> clazz);
}
