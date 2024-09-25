package com.yat;

import cn.hutool.core.lang.Dict;
import com.yat.cache.anno.api.CacheType;
import com.yat.cache.core.JetCache;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.template.QuickConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * ClassName TestServer
 * Description TestServer
 *
 * @author Yat
 * Date 2024/9/19 10:32
 * version 1.0
 */
@Service
public class TestServer implements InitializingBean {


    @Autowired
    private JetCacheManager cacheManager;
    @Autowired
    private UserService userService;

    private JetCache<String, String> orderCache;

    @Override
    public void afterPropertiesSet() {
        QuickConfig quickConfig = QuickConfig.newBuilder("orderCache:")
                .cacheType(CacheType.BOTH)
                .expire(Duration.ofSeconds(100))
                .build();
        orderCache = cacheManager.getOrCreateCache(quickConfig);
    }

    public void createCacheDemo() {
        orderCache.put("K1", "V1");
        System.out.println("get from orderCache:" + orderCache.get("K1"));
    }

    public String getCacheDemo() {
        return orderCache.get("K1");
    }

    public Dict cached() {
        return userService.loadUser(1);
    }
}
