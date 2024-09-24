package com.yat.cache.anno.config;

import com.yat.cache.anno.support.ConfigMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 通用配置类，用于定义通用的Bean
 * 该类被@Configuration注解标记，表示它是一个配置类，可以用来定义Spring容器中的Bean
 *
 * @author Yat
 */
@Configuration
public class CommonConfiguration {
    /**
     * 定义一个ConfigMap类型的Bean，用于cache配置
     * 该Bean被标记为基础设施角色，表示它在Spring的基础设施中使用，而不是直接在应用程序代码中使用
     *
     * @return 返回一个ConfigMap实例，用于存储JetCache的配置信息
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ConfigMap jetcacheConfigMap() {
        return new ConfigMap();
    }
}
