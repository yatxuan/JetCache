package com.yat;

import cn.hutool.core.lang.Dict;
import org.springframework.stereotype.Repository;

@Repository
public class UserServiceImpl implements UserService {

    @Override
    public Dict loadUser(long userId) {
        System.out.println("load user: " + userId);
        return Dict.create().set("id", userId).set("name", "user:" + userId);
    }
}
