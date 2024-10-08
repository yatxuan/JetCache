package com.yat.cache.anno.method;

import com.yat.cache.anno.support.ConfigMap;
import com.yat.cache.anno.support.ConfigProvider;
import com.yat.cache.core.JetCacheManager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * ProxyUtil类负责通过注解方式为给定目标对象创建代理对象，该代理对象能够处理缓存逻辑
 * 它主要通过处理目标对象的类型信息，提取出带有缓存注解的方法配置，并基于这些配置
 * 和缓存管理器来实现方法调用的拦截，以便在方法调用前后执行缓存相关操作
 *
 * @author Yat
 * Date 2024/8/22 22:01
 * version 1.0
 */
@SuppressWarnings("unused")
public class ProxyUtil {
    /**
     * 根据注解为给定目标对象创建代理对象
     *
     * @param target          目标对象，即需要为其创建代理的对象
     * @param configProvider  配置提供者，用于获取缓存配置上下文
     * @param jetCacheManager 缓存管理器，用于管理缓存实例和缓存逻辑
     * @param <T>             目标对象的泛型类型
     * @return 目标对象的代理对象，能够处理缓存逻辑
     */
    public static <T> T getProxyByAnnotation(T target, ConfigProvider configProvider, JetCacheManager jetCacheManager) {
        final ConfigMap configMap = new ConfigMap();
        processType(configMap, target.getClass());
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CacheHandler h = new CacheHandler(
                target, configMap,
                () -> configProvider.newContext(jetCacheManager).createCacheInvokeContext(configMap),
                configProvider.getGlobalCacheConfig().getHiddenPackages()
        );
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    /**
     * 处理给定类的信息，提取出带有缓存注解的方法配置
     * <p> 如果类是Java标准库中的类，则直接返回不处理</p>
     *
     * @param configMap 配置映射对象，用于存储方法的缓存配置
     * @param clazz     需要处理的类
     * @throws IllegalArgumentException 如果类是注解、数组、枚举或基本类型，则抛出异常
     */
    private static void processType(ConfigMap configMap, Class<?> clazz) {
        if (clazz.isAnnotation() || clazz.isArray() || clazz.isEnum() || clazz.isPrimitive()) {
            throw new IllegalArgumentException(clazz.getName());
        }
        if (clazz.getName().startsWith("java")) {
            return;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (Modifier.isPublic(m.getModifiers())) {
                processMethod(configMap, m);
            }
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> it : interfaces) {
            processType(configMap, it);
        }

        if (!clazz.isInterface()) {
            if (clazz.getSuperclass() != null) {
                processType(configMap, clazz.getSuperclass());
            }
        }
    }

    /**
     * 处理给定方法的缓存配置，将其提取并存储到配置映射对象中
     * <p>如果方法的缓存配置为空，则创建新的缓存配置对象，并将解析后的配置存储到映射中</p>
     *
     * @param configMap 配置映射对象，用于存储方法的缓存配置
     * @param m         需要处理的方法
     */
    private static void processMethod(ConfigMap configMap, Method m) {
        // 通过方法签名获取当前方法的唯一标识
        String sig = ClassUtil.getMethodSig(m);
        // 从配置映射中获取当前方法的缓存配置
        CacheInvokeConfig cac = configMap.getByMethodInfo(sig);

        // 如果当前方法没有缓存配置，则创建新的缓存配置对象
        if (cac == null) {
            cac = new CacheInvokeConfig();
            // 解析方法的缓存配置，如果成功则将其存储到配置映射中
            if (CacheConfigUtil.parse(cac, m)) {
                configMap.putByMethodInfo(sig, cac);
            }
        } else {
            // 如果当前方法已有缓存配置，则更新现有的配置
            CacheConfigUtil.parse(cac, m);
        }
    }

}
