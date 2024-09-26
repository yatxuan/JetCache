package com.yat;

import cn.hutool.json.JSONUtil;
import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.anno.config.EnableJetMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * ClassName JetCacheApplication
 * Description
 *
 * @author Yat
 * Date 2024/9/10 16:13
 * version 1.0
 */
@SpringBootApplication
@EnableJetMethodCache(basePackages = "com.yat")
public class JetCacheApplication {

    /**
     * 自定义key转换器
     */
    @Bean(DefaultCacheConstant.BEAN_KEY_CUSTOM)
    public KeyConvertor keyConvertor() {
        return JSONUtil::toJsonStr;
    }

    public static void main(String[] args) {
        SpringApplication.run(JetCacheApplication.class);
    }
}
