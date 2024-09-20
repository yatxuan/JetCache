package com.yat.cache.anno.support;

import com.yat.cache.anno.api.CacheType;
import com.yat.cache.core.RefreshPolicy;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author huangli
 */
public class CachedAnnoConfig extends CacheAnnoConfig {

    private boolean enabled;
    private TimeUnit timeUnit;
    private long expire;
    private long localExpire;
    private CacheType cacheType;
    private boolean syncLocal;
    private int localLimit;
    private boolean cacheNullValue;
    private String serialPolicy;
    private String keyConvertor;
    private String postCondition;

    private Function<Object, Boolean> postConditionEvaluator;
    private RefreshPolicy refreshPolicy;
    private PenetrationProtectConfig penetrationProtectConfig;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public int getLocalLimit() {
        return localLimit;
    }

    public void setLocalLimit(int localLimit) {
        this.localLimit = localLimit;
    }

    public boolean isCacheNullValue() {
        return cacheNullValue;
    }

    public void setCacheNullValue(boolean cacheNullValue) {
        this.cacheNullValue = cacheNullValue;
    }

    public String getSerialPolicy() {
        return serialPolicy;
    }

    public void setSerialPolicy(String serialPolicy) {
        this.serialPolicy = serialPolicy;
    }

    public String getKeyConvertor() {
        return keyConvertor;
    }

    public void setKeyConvertor(String keyConvertor) {
        this.keyConvertor = keyConvertor;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }


    public String getPostCondition() {
        return postCondition;
    }

    public void setPostCondition(String postCondition) {
        this.postCondition = postCondition;
    }

    public Function<Object, Boolean> getPostConditionEvaluator() {
        return postConditionEvaluator;
    }

    public void setPostConditionEvaluator(Function<Object, Boolean> postConditionEvaluator) {
        this.postConditionEvaluator = postConditionEvaluator;
    }

    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
    }

    public PenetrationProtectConfig getPenetrationProtectConfig() {
        return penetrationProtectConfig;
    }

    public void setPenetrationProtectConfig(PenetrationProtectConfig penetrationProtectConfig) {
        this.penetrationProtectConfig = penetrationProtectConfig;
    }

    public long getLocalExpire() {
        return localExpire;
    }

    public void setLocalExpire(long localExpire) {
        this.localExpire = localExpire;
    }

    public boolean isSyncLocal() {
        return syncLocal;
    }

    public void setSyncLocal(boolean syncLocal) {
        this.syncLocal = syncLocal;
    }
}
