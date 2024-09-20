/**
 * Created on  13-09-18 16:37
 */
package com.yat.cache.anno.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author huangli
 */
public class CacheNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("annotation-driven", new CacheAnnotationParser());
    }
}