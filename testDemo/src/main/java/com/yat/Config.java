package com.yat;

import cn.hutool.json.JSONUtil;
import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.api.KeyConvertor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName Config
 * Description 自定义配置
 *
 * @author Yat
 * Date 2024/9/25 16:14
 * version 1.0
 */
@Configuration
public class Config {

    @Bean(DefaultCacheConstant.BEAN_KEY_CUSTOM)
    public KeyConvertor keyConvertor() {
        return o -> {
            System.out.println("执行自定义key转换器");
            return JSONUtil.toJsonStr(o);
        };
    }
}
