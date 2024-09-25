package com.yat;

import cn.hutool.core.lang.Dict;
import com.yat.cache.anno.api.JetCachePenetrationProtect;
import com.yat.cache.anno.api.JetCacheRefresh;
import com.yat.cache.anno.api.JetCached;

public interface UserService {

    @JetCachePenetrationProtect
    @JetCacheRefresh(refresh = 5)
    @JetCached(area = "testA", name = "loadUser:", expire = 100,   keyConvertor = "GSON")
    Dict loadUser(long userId);
}
