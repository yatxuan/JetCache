/**
 * Created on 2019/6/23.
 */
package com.yat.cache.anno.config;

import com.yat.cache.anno.support.ConfigMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author huangli
 */
@Configuration
public class CommonConfiguration {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ConfigMap jetcacheConfigMap() {
        return new ConfigMap();
    }
}
