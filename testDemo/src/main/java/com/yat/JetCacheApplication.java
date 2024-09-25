package com.yat;

import com.google.gson.Gson;
import com.yat.cache.anno.config.EnableJetMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

    public static void main(String[] args) {
        new Gson().toJson("");

        SpringApplication.run(JetCacheApplication.class);
    }
}
