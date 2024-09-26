package com.yat.cache.core.support.encoders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * ClassName JavaValueDecoder
 * <p>Description  </p>
 * JavaValueDecoder 类继承自 AbstractValueDecoder，用于反序列化字节数组数据为 Java 对象。
 * 它实现了通过 ObjectInputStream 从字节数组中读取并还原为原始对象的功能。
 * 对于使用身份编号的配置，它会跳过前四个字节的身份编号，然后再进行反序列化。
 *
 * @author Yat
 * Date 2024/8/22 18:08
 * version 1.0
 */
public class JavaValueDecoder extends AbstractValueDecoder {

    /**
     * JavaValueDecoder 的单例实例，用于通用反序列化操作。
     */
    public static final JavaValueDecoder INSTANCE = new JavaValueDecoder(true);

    /**
     * 初始化父类的 useIdentityNumber 字段。
     */
    public JavaValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 反序列化逻辑。
     * 根据是否使用身份编号，决定处理字节数组的方式，并通过 ObjectInputStream 将字节数组反序列化为对象。
     *
     * @param buffer 待反序列化的字节数组。
     * @return 反序列化后的 Java 对象。
     * @throws Exception 反序列化过程中可能抛出的异常。
     */
    @Override
    public Object doApply(byte[] buffer) throws Exception {
        ByteArrayInputStream in;
        // 根据配置跳过身份编号部分。
        if (useIdentityNumber) {
            in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
        } else {
            in = new ByteArrayInputStream(buffer);
        }
        // 创建 ObjectInputStream 用于反序列化操作。
        ObjectInputStream ois = buildObjectInputStream(in);
        // 从输入流中读取并返回反序列化的对象。
        return ois.readObject();
    }

    /**
     * 构建 ObjectInputStream 的保护方法。
     * 子类可以覆盖此方法以提供自定义的 ObjectInputStream 实现。
     *
     * @param in ByteArrayInputStream 输入流。
     * @return ObjectInputStream 实例。
     * @throws IOException 如果无法创建 ObjectInputStream。
     */
    protected ObjectInputStream buildObjectInputStream(ByteArrayInputStream in) throws IOException {
        // 默认实现直接返回 ObjectInputStream 实例。
        return new ObjectInputStream(in);
    }
}
