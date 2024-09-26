package com.yat;

import cn.hutool.json.JSONUtil;
import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.anno.config.EnableJetMethodCache;
import com.yat.cache.autoconfigure.AutoConfigureBeans;
import com.yat.cache.autoconfigure.init.embedded.CaffeineAutoConfiguration;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.embedded.CaffeineCacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * ClassName Config
 * Description 自定义配置
 *
 * @author Yat
 * Date 2024/9/25 16:14
 * version 1.0
 */
@Configuration
@EnableJetMethodCache(basePackages = "com.*")
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Bean(DefaultCacheConstant.BEAN_KEY_CUSTOM)
    public KeyConvertor keyConvertor() {
        return o -> {
            System.out.println("执行自定义key转换器");
            return JSONUtil.toJsonStr(o);
        };
    }

    @Bean(DefaultCacheConstant.DEFAULT_LOCAL_LIMIT + "Caffeine")
    public CaffeineAutoConfiguration caffeineAutoConfiguration(@Autowired AutoConfigureBeans autoConfigureBeans) {

        String area = "testA";
        // String area = DefaultCacheConstant.DEFAULT_AREA;
        Map<String, CacheBuilder> localCacheBuilders = autoConfigureBeans.getLocalCacheBuilders();
        if (localCacheBuilders.containsKey(area)) {
            return null;
        }
        CaffeineCacheBuilder.CaffeineCacheBuilderImpl builder =
                CaffeineCacheBuilder.createCaffeineCacheBuilder();

        builder.keyConvertor(KeyConvertor.NONE_INSTANCE)
                .limit(DefaultCacheConstant.DEFAULT_LOCAL_LIMIT);

        localCacheBuilders.put(area, builder);

        return null;
    }
}
