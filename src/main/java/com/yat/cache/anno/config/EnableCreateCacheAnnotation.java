package com.yat.cache.anno.config;

/**
 * Created on 2016/12/13.
 *
 * @author huangli
 * @deprecated CreateCache annotation is replaced by CacheManager.getOrCreateCache(QuickConfig), the CacheManager
 * instance
 * can be injected use annotation such as @Autowired.
 */

import com.yat.cache.anno.field.CreateCacheAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CommonConfiguration.class, CreateCacheAnnotationBeanPostProcessor.class})
@Deprecated
public @interface EnableCreateCacheAnnotation {
}
