package com.yat;

import cn.hutool.core.lang.Dict;
import com.yat.utils.JetCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private UserService userService;

    public void createCacheDemo() {
        JetCacheUtil.put("K1", "V1");
        System.out.println("get from orderCache:" + JetCacheUtil.get("K1"));
    }

    public String getCacheDemo() {
        return JetCacheUtil.get("K1");
    }

    public Dict cached() {
        return userService.loadUser(1);
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
