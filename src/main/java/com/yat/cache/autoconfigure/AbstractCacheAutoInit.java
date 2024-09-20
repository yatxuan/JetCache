package com.yat.cache.autoconfigure;

import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.anno.support.ParserFunction;
import com.yat.cache.core.AbstractCacheBuilder;
import com.yat.cache.core.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 2016/11/29.
 *
 * @author huangli
 */
public abstract class AbstractCacheAutoInit implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(AbstractCacheAutoInit.class);
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private volatile boolean inited = false;
    @Autowired
    protected ConfigurableEnvironment environment;
    @Autowired
    protected AutoConfigureBeans autoConfigureBeans;
    protected String[] typeNames;

    public AbstractCacheAutoInit(String... cacheTypes) {
        Objects.requireNonNull(cacheTypes,"cacheTypes can't be null");
        Assert.isTrue(cacheTypes.length > 0, "cacheTypes length is 0");
        this.typeNames = cacheTypes;
    }

    @Override
    public void afterPropertiesSet() {
        if (!inited) {
            reentrantLock.lock();
            try{
                if (!inited) {
                    process("jetcache.local.", autoConfigureBeans.getLocalCacheBuilders(), true);
                    process("jetcache.remote.", autoConfigureBeans.getRemoteCacheBuilders(), false);
                    inited = true;
                }
            }finally {
                reentrantLock.unlock();
            }
        }
    }

    private void process(String prefix, Map cacheBuilders, boolean local) {
        ConfigTree resolver = new ConfigTree(environment, prefix);
        Map<String, Object> m = resolver.getProperties();
        Set<String> cacheAreaNames = resolver.directChildrenKeys();
        for (String cacheArea : cacheAreaNames) {
            final Object configType = m.get(cacheArea + ".type");
            boolean match = Arrays.stream(typeNames).anyMatch((tn) -> tn.equals(configType));
            if (!match) {
                continue;
            }
            ConfigTree ct = resolver.subTree(cacheArea + ".");
            logger.info("init cache area {} , type= {}", cacheArea, typeNames[0]);
            CacheBuilder c = initCache(ct, local ? "local." + cacheArea : "remote." + cacheArea);
            cacheBuilders.put(cacheArea, c);
        }
    }

    protected abstract CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix);

    protected void parseGeneralConfig(CacheBuilder builder, ConfigTree ct) {
        AbstractCacheBuilder acb = (AbstractCacheBuilder) builder;
        acb.keyConvertor(new ParserFunction(ct.getProperty("keyConvertor", KeyConvertor.GSON)));

        String expireAfterWriteInMillis = ct.getProperty("expireAfterWriteInMillis");
        if (expireAfterWriteInMillis == null) {
            // compatible with 2.1
            expireAfterWriteInMillis = ct.getProperty("defaultExpireInMillis");
        }
        if (expireAfterWriteInMillis != null) {
            acb.setExpireAfterWriteInMillis(Long.parseLong(expireAfterWriteInMillis));
        }

        String expireAfterAccessInMillis = ct.getProperty("expireAfterAccessInMillis");
        if (expireAfterAccessInMillis != null) {
            acb.setExpireAfterAccessInMillis(Long.parseLong(expireAfterAccessInMillis));
        }

    }
}
