package com.yat.cache.anno.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.function.Function;

/**
 * ClassName DefaultSpringKeyConvertorParser
 * <p>Description 基于Spring的KeyConvertor解析器</p>
 *
 * @author Yat
 * Date 2024/8/22 22:05
 * version 1.0
 */
public class DefaultSpringKeyConvertorParser extends DefaultKeyConvertorParser implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Function<Object, Object> parseKeyConvertor(String convertor) {
        String beanName = DefaultSpringEncoderParser.parseBeanName(convertor);
        if (beanName == null) {
            return super.parseKeyConvertor(convertor);
        } else {
            return (Function<Object, Object>) applicationContext.getBean(beanName);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
