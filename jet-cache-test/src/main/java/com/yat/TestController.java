package com.yat;

import cn.hutool.core.lang.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName TestController
 * Description TestController
 *
 * @author Yat
 * Date 2024/9/19 10:31
 * version 1.0
 */
@RestController
@RequestMapping("/test")
public class TestController {

    private TestServer testServer;

    @GetMapping("/cached")
    public Dict cached() {
        return testServer.cached();
    }

    @GetMapping("/cache")
    public String cache() {
        testServer.createCacheDemo();
        return "success";
    }

    @GetMapping("/getCache")
    public String getCache() {
        return testServer.getCacheDemo();
    }

    @Autowired
    public void setTestServer(TestServer testServer) {
        this.testServer = testServer;
    }
}
