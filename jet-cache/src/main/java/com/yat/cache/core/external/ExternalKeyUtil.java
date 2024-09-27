package com.yat.cache.core.external;

import com.yat.cache.core.exception.CacheException;
import com.yat.cache.core.lang.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ClassName ExternalKeyUtil
 * <p>Description 缓存键构建工具类:用于构建缓存键的方法，支持多种类型的键转换</p>
 *
 * @author Yat
 * Date 2024/8/22 13:41
 * version 1.0
 */
public class ExternalKeyUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss,SSS");

    /**
     * 根据给定的新键和前缀构建缓存键。
     *
     * @param newKey 新键。
     * @param prefix 前缀。
     * @return 构建后的缓存键。
     * @throws IOException 如果发生 I/O 异常。
     */
    public static byte[] buildKeyAfterConvert(Object newKey, String prefix) throws IOException {

        Assert.notNull(newKey, () -> new NullPointerException("key can't be null"));

        byte[] keyBytesWithOutPrefix;
        if (newKey instanceof String) {
            keyBytesWithOutPrefix = newKey.toString().getBytes(StandardCharsets.UTF_8);
        } else if (newKey instanceof byte[]) {
            keyBytesWithOutPrefix = (byte[]) newKey;
        } else if (newKey instanceof Number) {
            keyBytesWithOutPrefix = (newKey.getClass().getSimpleName() + newKey).getBytes(StandardCharsets.UTF_8);
        } else if (newKey instanceof Date) {
            keyBytesWithOutPrefix = (newKey.getClass().getSimpleName() + sdf.format(newKey)).getBytes();
        } else if (newKey instanceof Boolean) {
            keyBytesWithOutPrefix = newKey.toString().getBytes(StandardCharsets.UTF_8);
        } else if (newKey instanceof Serializable) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(newKey);
            os.close();
            bos.close();
            keyBytesWithOutPrefix = bos.toByteArray();
        } else {
            // throw new CacheException("无法转换 " + newKey.getClass() + " 类型的键");
            throw new CacheException("can't convert key of class: " + newKey.getClass());
        }
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        byte[] rt = new byte[prefixBytes.length + keyBytesWithOutPrefix.length];
        System.arraycopy(prefixBytes, 0, rt, 0, prefixBytes.length);
        System.arraycopy(keyBytesWithOutPrefix, 0, rt, prefixBytes.length, keyBytesWithOutPrefix.length);
        return rt;
    }
}
