package com.yat.cache.core.support;

import com.yat.cache.core.support.encoders.JavaValueDecoder;
import org.springframework.core.ConfigurableObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * ClassName SpringJavaValueDecoder
 * <p>Description SpringJavaValueDecoder是JavaValueDecoder的扩展，专门用于在Spring环境中反序列化对象。</p>
 * 它通过使用ConfigurableObjectInputStream来允许在不同的类加载器之间进行反序列化操作。
 * 这对于在分布式系统或微服务架构中处理由Spring框架管理的对象特别有用。
 *
 * @author Yat
 * Date 2024/8/22 19:56
 * version 1.0
 */
public class SpringJavaValueDecoder extends JavaValueDecoder {

    /**
     * INSTANCE作为SpringJavaValueDecoder的静态实例，便于在需要时重复使用。
     */
    public static final SpringJavaValueDecoder INSTANCE = new SpringJavaValueDecoder(true);

    /**
     * 调用父类JavaValueDecoder的根据参数初始化当前对象。
     */
    public SpringJavaValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    /**
     * 重写buildObjectInputStream方法，用以返回一个ConfigurableObjectInputStream实例，
     * 而不是标准的ObjectInputStream。这样做允许我们指定用于反序列化的类加载器，
     * 在这里我们使用了当前线程的上下文类加载器。
     *
     * @param in 用于反序列化的字节输入流。
     * @return 一个新的ConfigurableObjectInputStream实例，用于反序列化。
     * @throws IOException 如果无法创建ObjectInputStream。
     */
    @Override
    protected ObjectInputStream buildObjectInputStream(ByteArrayInputStream in) throws IOException {
        return new ConfigurableObjectInputStream(in, Thread.currentThread().getContextClassLoader());
    }
}
