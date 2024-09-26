package com.yat;

import cn.hutool.core.lang.Dict;
import com.yat.cache.anno.api.CacheType;
import com.yat.cache.anno.api.EnableJetCache;
import com.yat.cache.anno.api.JetCached;

public interface UserService {

    @EnableJetCache
    // @JetCachePenetrationProtect
    // @JetCacheRefresh(refresh = 50)
    @JetCached(area = "testA", name = "loadUser:", expire = 100, cacheType = CacheType.BOTH, keyConvertor = "GSON")
    Dict loadUser(long userId);
}
