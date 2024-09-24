package com.yat.cache.anno.support;

import com.yat.cache.anno.api.SerialPolicy;
import com.yat.cache.core.support.SpringJavaValueDecoder;
import com.yat.cache.core.support.encoders.JavaValueDecoder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.function.Function;

/**
 * DefaultSpringEncoderParser类是DefaultEncoderParser的子类，也是Spring上下文的一个组件。
 * 它的作用是提供编码器和解码器的解析，用于序列化和反序列化对象。
 * 在Spring环境中，它可以通过应用程序上下文获取序列化策略相关的Bean。
 *
 * @author Yat
 * Date 2024/8/22 22:05
 * version 1.0
 */
public class DefaultSpringEncoderParser extends DefaultEncoderParser implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 解析给定的SerialPolicyTypeEnum值，返回相应的编码器函数。
     * 如果指定的编码器在Spring上下文中配置为Bean，则从上下文中获取该Bean。
     *
     * @param valueEncoder 序列化策略类型枚举，用于确定使用哪种编码器。
     * @return 编码器函数，将对象序列化为字节数组。
     */
    @Override
    public Function<Object, byte[]> parseEncoder(String valueEncoder) {
        String beanName = parseBeanName(valueEncoder);
        if (beanName == null) {
            return super.parseEncoder(valueEncoder);
        } else {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof Function) {
                return (Function<Object, byte[]>) bean;
            } else {
                return ((SerialPolicy) bean).encoder();
            }
        }
    }

    /**
     * 解析字符串，如果是以"bean:"前缀开始，则返回去掉前缀后的字符串。
     * 用于从Spring上下文中获取序列化策略相关的Bean。
     *
     * @param str 待解析的字符串。
     * @return 解析后的Bean名称，如果字符串不匹配则返回null。
     */
    static String parseBeanName(String str) {
        final String beanPrefix = "BEAN_";
        int len = beanPrefix.length();
        if (str != null && str.startsWith(beanPrefix) && str.length() > len) {
            return str;
        } else {
            return null;
        }
    }

    /**
     * 解析给定的SerialPolicyTypeEnum值，返回相应的解码器函数。
     * 类似于parseEncoder方法，但专注于解码操作。
     *
     * @param valueDecoder 序列化策略类型枚举，用于确定使用哪种解码器。
     * @return 解码器函数，将字节数组反序列化为对象。
     */
    @Override
    public Function<byte[], Object> parseDecoder(String valueDecoder) {
        String beanName = parseBeanName(valueDecoder);
        if (beanName == null) {
            return super.parseDecoder(valueDecoder);
        } else {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof Function) {
                return (Function<byte[], Object>) bean;
            } else {
                return ((SerialPolicy) bean).decoder();
            }
        }
    }

    /**
     * 根据是否使用唯一标识来获取适当的JavaValueDecoder实现。
     * 在Spring环境中，使用SpringJavaValueDecoder。
     *
     * @param useIdentityNumber 如果true，则在序列化时使用对象的唯一标识。
     * @return JavaValueDecoder的实例。
     */
    @Override
    JavaValueDecoder javaValueDecoder(boolean useIdentityNumber) {
        return new SpringJavaValueDecoder(useIdentityNumber);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
