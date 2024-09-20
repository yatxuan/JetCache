/**
 * Created on  13-10-02 16:10
 */
package com.yat.cache.anno.method;

import com.yat.cache.anno.support.CacheAnnoConfig;
import com.yat.cache.core.Cache;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * @author huangli
 */
public class CacheInvokeContext {
    private Invoker invoker;
    private Method method;
    private Object[] args;
    private CacheInvokeConfig cacheInvokeConfig;
    private Object targetObject;
    private Object result;

    private BiFunction<CacheInvokeContext, CacheAnnoConfig, Cache> cacheFunction;
    private String[] hiddenPackages;

    public CacheInvokeContext() {
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public CacheInvokeConfig getCacheInvokeConfig() {
        return cacheInvokeConfig;
    }

    public void setCacheInvokeConfig(CacheInvokeConfig cacheInvokeConfig) {
        this.cacheInvokeConfig = cacheInvokeConfig;
    }

    public String[] getHiddenPackages() {
        return hiddenPackages;
    }

    public void setHiddenPackages(String[] hiddenPackages) {
        this.hiddenPackages = hiddenPackages;
    }

    public BiFunction<CacheInvokeContext, CacheAnnoConfig, Cache> getCacheFunction() {
        return cacheFunction;
    }

    public void setCacheFunction(BiFunction<CacheInvokeContext, CacheAnnoConfig, Cache> cacheFunction) {
        this.cacheFunction = cacheFunction;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
