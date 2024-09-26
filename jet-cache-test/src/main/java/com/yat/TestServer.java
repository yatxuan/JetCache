package com.yat;

import cn.hutool.core.lang.Dict;
import com.yat.cache.anno.api.CacheType;
import com.yat.cache.core.JetCache;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.template.QuickConfig;
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
public class TestServer {

    private JetCacheManager cacheManager;
    private UserService userService;

    public void createCacheDemo() {
        getOrderCache().put("K1", "V1");
        System.out.println("get from orderCache:" + getOrderCache().get("K1"));
    }

    public JetCache<String, String> getOrderCache() {
        QuickConfig quickConfig = QuickConfig.newBuilder("orderCache:")
                .cacheType(CacheType.BOTH)
                .expire(Duration.ofSeconds(100))
                .build();
        return cacheManager.getOrCreateCache(quickConfig);
    }

    public String getCacheDemo() {
        return getOrderCache().get("K1");
    }

    public Dict cached() {
        return userService.loadUser(1);
    }

    @Autowired
    public void setCacheManager(JetCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
