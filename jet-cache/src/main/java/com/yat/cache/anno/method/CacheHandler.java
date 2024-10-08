package com.yat.cache.anno.method;

import com.yat.cache.anno.support.CacheContext;
import com.yat.cache.anno.support.CacheInvalidateAnnoConfig;
import com.yat.cache.anno.support.CacheUpdateAnnoConfig;
import com.yat.cache.anno.support.CachedAnnoConfig;
import com.yat.cache.anno.support.ConfigMap;
import com.yat.cache.core.AbstractJetCache;
import com.yat.cache.core.CacheLoader;
import com.yat.cache.core.JetCache;
import com.yat.cache.core.ProxyJetCache;
import com.yat.cache.core.event.CacheLoadEvent;
import com.yat.cache.core.exception.CacheInvokeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * ClassName CacheHandler
 * <p>Description 缓存处理程序</p>
 *
 * @author Yat
 * Date 2024/8/22 21:44
 * version 1.0
 */
public class CacheHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(CacheHandler.class);

    private final Object src;
    private final Supplier<CacheInvokeContext> contextSupplier;
    private final String[] hiddenPackages;
    private final ConfigMap configMap;

    public CacheHandler(Object src, ConfigMap configMap, Supplier<CacheInvokeContext> contextSupplier,
                        String[] hiddenPackages) {
        this.src = src;
        this.configMap = configMap;
        this.contextSupplier = contextSupplier;
        this.hiddenPackages = hiddenPackages;
    }

    /**
     * 代理方法调用处理
     *
     * @param proxy  代理对象
     * @param method 调用的方法
     * @param args   方法参数
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        CacheInvokeContext context = null;

        String sig = ClassUtil.getMethodSig(method);
        CacheInvokeConfig cac = configMap.getByMethodInfo(sig);
        if (cac != null) {
            context = contextSupplier.get();
            context.setCacheInvokeConfig(cac);
        }
        if (context == null) {
            return method.invoke(src, args);
        } else {
            context.setInvoker(() -> method.invoke(src, args));
            context.setHiddenPackages(hiddenPackages);
            context.setArgs(args);
            context.setMethod(method);
            return invoke(context);
        }
    }

    /**
     * 静态方法调用处理
     *
     * @param context 调用上下文
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    public static Object invoke(CacheInvokeContext context) throws Throwable {
        if (context.getCacheInvokeConfig().isEnableCacheContext()) {
            try {
                CacheContextSupport._enable();
                return doInvoke(context);
            } finally {
                CacheContextSupport._disable();
            }
        } else {
            return doInvoke(context);
        }
    }

    /**
     * 实际方法调用处理
     *
     * @param context 调用上下文
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    private static Object doInvoke(CacheInvokeContext context) throws Throwable {
        CacheInvokeConfig cic = context.getCacheInvokeConfig();
        CachedAnnoConfig cachedConfig = cic.getCachedAnnoConfig();
        if (cachedConfig != null && (cachedConfig.isEnabled() || CacheContextSupport._isEnabled())) {
            return invokeWithCached(context);
        } else if (cic.getInvalidateAnnoConfigs() != null || cic.getUpdateAnnoConfig() != null) {
            return invokeWithInvalidateOrUpdate(context);
        } else {
            return invokeOrigin(context);
        }
    }

    /**
     * 带缓存的方法调用处理
     *
     * @param context 调用上下文
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    private static Object invokeWithCached(CacheInvokeContext context)
            throws Throwable {
        CacheInvokeConfig cic = context.getCacheInvokeConfig();
        CachedAnnoConfig cac = cic.getCachedAnnoConfig();
        JetCache jetCache = context.getCacheFunction().apply(context, cac);
        if (jetCache == null) {
            logger.error("no cache with name: {}", context.getMethod());
            return invokeOrigin(context);
        }

        Object key = ExpressionUtil.evalKey(context, cic.getCachedAnnoConfig());
        if (key == null) {
            return loadAndCount(context, jetCache, null);
        }

        if (!ExpressionUtil.evalCondition(context, cic.getCachedAnnoConfig())) {
            return loadAndCount(context, jetCache, key);
        }

        try {
            CacheLoader loader = new CacheLoader<>() {
                @Override
                public Object load(Object k) throws Throwable {
                    Object result = invokeOrigin(context);
                    context.setResult(result);
                    return result;
                }

                @Override
                public boolean vetoCacheUpdate() {
                    return !ExpressionUtil.evalPostCondition(context, cic.getCachedAnnoConfig());
                }
            };
            return jetCache.computeIfAbsent(key, loader);
        } catch (CacheInvokeException e) {
            throw e.getCause();
        }
    }

    /**
     * 执行带有失效或更新操作的调用
     * 该方法首先执行原始方法调用，然后根据配置进行缓存失效或更新操作
     *
     * @param context 缓存调用上下文
     * @return 原始调用的结果
     * @throws Throwable 如果原始调用过程中发生错误
     */
    private static Object invokeWithInvalidateOrUpdate(CacheInvokeContext context) throws Throwable {
        Object originResult = invokeOrigin(context);
        context.setResult(originResult);
        CacheInvokeConfig cic = context.getCacheInvokeConfig();

        if (cic.getInvalidateAnnoConfigs() != null) {
            doInvalidate(context, cic.getInvalidateAnnoConfigs());
        }
        CacheUpdateAnnoConfig updateAnnoConfig = cic.getUpdateAnnoConfig();
        if (updateAnnoConfig != null) {
            doUpdate(context, updateAnnoConfig);
        }

        return originResult;
    }

    /**
     * 执行原始的缓存调用
     * 该方法负责实际执行缓存中的方法调用
     *
     * @param context 缓存调用上下文
     * @return 原始调用的结果
     * @throws Throwable 如果调用过程中发生错误
     */
    private static Object invokeOrigin(CacheInvokeContext context) throws Throwable {
        return context.getInvoker().invoke();
    }

    /**
     * 加载值并统计时间
     * 该方法执行缓存调用，记录执行时间，并触发缓存加载事件
     *
     * @param context  缓存调用上下文
     * @param jetCache 缓存实例
     * @param key      缓存键
     * @return 缓存调用的结果
     * @throws Throwable 如果调用过程中发生错误
     */
    private static Object loadAndCount(CacheInvokeContext context, JetCache jetCache, Object key) throws Throwable {
        long t = System.currentTimeMillis();
        Object v = null;
        boolean success = false;
        try {
            v = invokeOrigin(context);
            success = true;
        } finally {
            t = System.currentTimeMillis() - t;
            CacheLoadEvent event = new CacheLoadEvent(jetCache, t, key, v, success);
            while (jetCache instanceof ProxyJetCache) {
                jetCache = ((ProxyJetCache) jetCache).getTargetCache();
            }
            if (jetCache instanceof AbstractJetCache) {
                ((AbstractJetCache) jetCache).notify(event);
            }
        }
        return v;
    }

    /**
     * 执行缓存失效操作
     * 遍历缓存失效配置列表，逐个执行缓存失效操作
     *
     * @param context    缓存调用上下文
     * @param annoConfig 缓存失效注解配置列表
     */
    private static void doInvalidate(CacheInvokeContext context, List<CacheInvalidateAnnoConfig> annoConfig) {
        for (CacheInvalidateAnnoConfig config : annoConfig) {
            doInvalidate(context, config);
        }
    }

    /**
     * 将给定的键值对更新到缓存中。
     *
     * @param context          缓存调用上下文，用于获取缓存函数和评估条件、值、键等。
     * @param updateAnnoConfig 更新配置，包含缓存更新的详细配置信息，如键、值、条件等。
     */
    private static void doUpdate(CacheInvokeContext context, CacheUpdateAnnoConfig updateAnnoConfig) {
        // 根据上下文和更新配置获取缓存实例
        try (JetCache jetCache = context.getCacheFunction().apply(context, updateAnnoConfig)) {
            // 如果缓存为null，则直接返回
            if (jetCache == null) {
                return;
            }
            // 评估更新缓存的条件
            boolean condition = ExpressionUtil.evalCondition(context, updateAnnoConfig);
            // 如果条件不满足，则直接返回
            if (!condition) {
                return;
            }

            // 评估缓存更新的值
            Object value = ExpressionUtil.evalValue(context, updateAnnoConfig);
            // 评估缓存更新的键
            Object key = ExpressionUtil.evalKey(context, updateAnnoConfig);
            // 如果键为null或值评估失败，则直接返回
            if (key == null || value == ExpressionUtil.EVAL_FAILED) {
                return;
            }
            // 如果是批量更新
            if (updateAnnoConfig.isMulti()) {
                // 如果值为null，则直接返回
                if (value == null) {
                    return;
                }
                // 将键和值转换为可迭代对象
                Iterable<Object> keyIt = toIterable(key);
                Iterable<Object> valueIt = toIterable(value);
                // 如果键不是Iterable或数组类型，则记录错误并返回
                if (keyIt == null) {
                    logger.error(
                            "JetCache @CacheUpdate key is not instance of Iterable or array: {}",
                            updateAnnoConfig.getDefineMethod()
                    );
                    return;
                }
                // 如果值不是Iterable或数组类型，则记录错误并返回
                if (valueIt == null) {
                    logger.error(
                            "JetCache @CacheUpdate value is not instance of Iterable or array: {}",
                            updateAnnoConfig.getDefineMethod()
                    );
                    return;
                }

                // 将键和值的可迭代对象转换为列表
                List<Object> keyList = new ArrayList<>();
                List<Object> valueList = new ArrayList<>();
                keyIt.forEach(keyList::add);
                valueIt.forEach(valueList::add);
                // 如果键和值的列表长度不一致，则记录错误并返回
                if (keyList.size() != valueList.size()) {
                    logger.error(
                            "JetCache @CacheUpdate key size not equals with value size: {}",
                            updateAnnoConfig.getDefineMethod()
                    );
                } else {
                    // 创建一个映射来存储键值对关系
                    Map<Object, Object> m = new HashMap<>();
                    // 遍历键值列表，将键值对添加到映射中
                    for (int i = 0; i < valueList.size(); i++) {
                        m.put(keyList.get(i), valueList.get(i));
                    }
                    // 将所有键值对批量更新到缓存中
                    jetCache.putAll(m);
                }
            } else {
                // 对于非批量更新，直接将键值对更新到缓存中
                jetCache.put(key, value);
            }
        }
    }

    /**
     * 执行实际的缓存无效操作。
     *
     * @param context    缓存调用上下文
     * @param annoConfig 无效配置
     */
    private static void doInvalidate(CacheInvokeContext context, CacheInvalidateAnnoConfig annoConfig) {
        try (JetCache jetCache = context.getCacheFunction().apply(context, annoConfig)) {
            if (jetCache == null) {
                return;
            }
            boolean condition = ExpressionUtil.evalCondition(context, annoConfig);
            if (!condition) {
                return;
            }
            Object key = ExpressionUtil.evalKey(context, annoConfig);
            if (key == null) {
                return;
            }
            if (annoConfig.isMulti()) {
                Iterable it = toIterable(key);
                if (it == null) {
                    logger.error("JetCache @CacheInvalidate key is not instance of Iterable or array: {}",
                            annoConfig.getDefineMethod());
                    return;
                }
                Set keys = new HashSet<>();
                it.forEach(keys::add);
                jetCache.removeAll(keys);
            } else {
                jetCache.remove(key);
            }
        }
    }

    /**
     * 将对象转换为Iterable类型
     * 支持将数组或Iterable类型对象转换为Iterable，否则返回null
     *
     * @param obj 需要转换的对象
     * @return 转换后的Iterable对象，如果转换失败则返回null
     */
    private static Iterable<Object> toIterable(Object obj) {
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return Arrays.asList((Object[]) obj);
            } else {
                List<Object> list = new ArrayList<>();
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++) {
                    list.add(Array.get(obj, i));
                }
                return list;
            }
        } else if (obj instanceof Iterable) {
            return (Iterable) obj;
        } else {
            return null;
        }
    }

    /**
     * 支持缓存上下文的操作类。
     */
    private static class CacheContextSupport extends CacheContext {

        public CacheContextSupport() {
            super(null, null, null);
        }


        /**
         * 启用缓存上下文。
         */
        static void _enable() {
            enable();
        }

        /**
         * 禁用缓存上下文。
         */
        static void _disable() {
            disable();
        }

        /**
         * 检查缓存上下文是否启用。
         *
         * @return 是否启用
         */
        static boolean _isEnabled() {
            return isEnabled();
        }
    }

}
