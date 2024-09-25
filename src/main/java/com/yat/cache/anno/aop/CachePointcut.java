package com.yat.cache.anno.aop;

import com.yat.cache.anno.method.CacheConfigUtil;
import com.yat.cache.anno.method.CacheInvokeConfig;
import com.yat.cache.anno.method.ClassUtil;
import com.yat.cache.anno.support.ConfigMap;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.asm.Type;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * ClassName CachePointcut
 * <p>Description CachePointcut</p>
 *
 * @author Yat
 * Date 2024/9/23 12:01
 * version 1.0
 */
public class CachePointcut extends StaticMethodMatcherPointcut implements ClassFilter {

    private static final Logger logger = LoggerFactory.getLogger(CachePointcut.class);
    private final String[] basePackages;
    /**
     * 设置缓存配置映射
     */
    @Setter
    private ConfigMap cacheConfigMap;

    public CachePointcut(String[] basePackages) {
        setClassFilter(this);
        this.basePackages = basePackages;
    }

    /**
     * 检查给定的类是否与某个条件匹配
     * 此方法通过调用内部实现方法{@code matchesImpl}来实际执行匹配，并记录匹配结果和被检查的类信息
     *
     * @param clazz 要检查的类，可以是任意类型的类
     * @return 匹配结果，如果类匹配则返回true，否则返回false
     */
    @Override
    public boolean matches(@NonNull Class<?> clazz) {
        // 调用matchesImpl方法执行匹配逻辑，结果用于决定是否返回true
        boolean b = matchesImpl(clazz);
        // 记录匹配结果和被检查的类信息，帮助调试和追踪
        logger.trace("check class match {}: {}", b, clazz);
        // 返回匹配结果
        return b;
    }


    /**
     * 核心匹配逻辑
     * 此方法用于判断给定的类是否匹配某些预设的类条件
     * 它会首先检查该类是否直接满足匹配条件，然后检查其接口，最后检查其父类
     *
     * @param clazz 要匹配的类
     * @return 如果类匹配则返回true，否则返回false
     */
    private boolean matchesImpl(Class<?> clazz) {
        // 检查当前类是否直接满足匹配条件
        if (matchesThis(clazz)) {
            return Boolean.TRUE;
        }

        // 获取类实现的所有接口
        Class<?>[] cs = clazz.getInterfaces();
        // 遍历所有接口，递归检查接口是否匹配条件
        if (ObjectUtils.isArray(cs)) {
            for (Class<?> c : cs) {
                if (matchesImpl(c)) {
                    return Boolean.TRUE;
                }
            }
        }

        // 如果类不是接口，则检查其父类是否匹配条件
        if (!clazz.isInterface()) {
            Class<?> sp = clazz.getSuperclass();
            // 如果有父类且父类匹配条件，则返回true
            if (sp != null && matchesImpl(sp)) {
                return Boolean.TRUE;
            }
        }

        // 如果没有找到匹配的类或接口，则返回false
        return Boolean.FALSE;
    }

    /**
     * 判断是否匹配指定的类
     * 此方法首先通过获取类的全名，然后检查该名称是否在排除列表中
     * 如果在排除列表中，则返回false；否则，根据类的名称判断是否在包含列表中，并返回相应的结果
     *
     * @param clazz 要检查的类对象
     * @return 如果类的名称在包含列表中且不在排除列表中，则返回true；否则返回false
     */
    public boolean matchesThis(Class<?> clazz) {
        // 获取类的全名
        String name = clazz.getName();

        // 检查类的名称是否在排除列表中
        if (exclude(name)) {
            // 如果在排除列表中，直接返回false
            return Boolean.FALSE;
        }

        // 检查类的名称是否在包含列表中，并返回相应的结果
        return include(name);
    }

    /**
     * 判断给定的名称是否在配置的基础包范围内
     *
     * @param name 待检查的名称
     * @return 如果名称在基础包范围内，则返回true；否则返回false
     */
    private boolean include(String name) {
        // 如果基础包列表不为空
        if (basePackages != null) {
            // 遍历基础包列表
            for (String p : basePackages) {
                // 如果名称以基础包开始
                if (name.startsWith(p)) {
                    // 则认为该名称在配置范围内，返回true
                    return Boolean.TRUE;
                }
            }
        }
        // 如果遍历完没有匹配的基础包，或者基础包列表为空，则认为该名称不在配置范围内，返回false
        return Boolean.FALSE;
    }

    /**
     * 判断一个类名是否符合排除条件
     *
     * @param name 待判断的类名
     * @return 如果类名符合排除条件，则返回true；否则返回false
     * <p>
     * <p>
     * 排除条件包括：
     * 1. 类名以"java"开头，这通常表示它是Java标准库的一部分，可能需要特殊处理或避免干扰
     * 2. 类名以"org.springframework"开头，这通常表示它是Spring框架的核心组件，同样需要特殊处理
     * 3. 类名包含"$$EnhancerBySpringCGLIB$$"，这通常表示它是Spring CGLIB动态代理生成的类，为了防止代理类的影响，需要排除
     * 4. 类名包含"$$FastClassBySpringCGLIB$$"，这通常表示它是Spring CGLIB优化的类，同样需要识别并排除
     */
    private boolean exclude(String name) {
        if (name.startsWith("java")) {
            return Boolean.TRUE;
        }
        if (name.startsWith("org.springframework")) {
            return Boolean.TRUE;
        }
        if (name.contains("$$EnhancerBySpringCGLIB$$")) {
            return Boolean.TRUE;
        }
        if (name.contains("$$FastClassBySpringCGLIB$$")) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        // 根据给定的方法和目标类检查是否匹配
        // 使用matchesImpl方法来实际判断是否匹配
        boolean b = matchesImpl(method, targetClass);

        // 如果匹配成功，则记录调试信息
        if (b) {
            if (logger.isDebugEnabled()) {
                logger.debug("check method match true: method={}, declaringClass={}, targetClass={}",
                        method.getName(),
                        ClassUtil.getShortClassName(method.getDeclaringClass().getName()),
                        ClassUtil.getShortClassName(targetClass.getName()));
            }
        } else {
            // 如果不匹配，则记录追踪信息
            if (logger.isTraceEnabled()) {
                logger.trace("check method match false: method={}, declaringClass={}, targetClass={}",
                        method.getName(),
                        ClassUtil.getShortClassName(method.getDeclaringClass().getName()),
                        ClassUtil.getShortClassName(targetClass.getName()));
            }
        }
        // 返回匹配结果
        return b;
    }

    /**
     * 判断指定方法和目标类是否符合缓存配置的条件
     *
     * @param method      被检查的方法
     * @param targetClass 目标类
     * @return 如果符合缓存配置条件返回true，否则返回false
     */
    private boolean matchesImpl(Method method, Class<?> targetClass) {
        // 检查方法的声明类是否符合缓存匹配条件
        if (!matchesThis(method.getDeclaringClass())) {
            return Boolean.FALSE;
        }
        // 检查目标类是否在排除列表中
        if (exclude(targetClass.getName())) {
            return Boolean.FALSE;
        }
        // 生成当前方法和目标类的缓存键
        String key = getKey(method, targetClass);
        // 通过缓存键获取缓存的配置信息
        CacheInvokeConfig cac = cacheConfigMap.getByMethodInfo(key);
        // 如果缓存配置指示不使用缓存，则返回false
        if (cac == CacheInvokeConfig.getNoCacheInvokeConfigInstance()) {
            return Boolean.FALSE;
        } else if (cac != null) {
            // 如果缓存配置存在，则返回true
            return Boolean.TRUE;
        } else {
            // 如果缓存配置不存在，初始化一个新的缓存配置对象
            cac = new CacheInvokeConfig();
            // 解析方法级别的缓存配置
            CacheConfigUtil.parse(cac, method);

            // 获取方法名和参数类型数组
            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            // 根据目标类解析缓存配置
            parseByTargetClass(cac, targetClass, name, paramTypes);

            // 检查缓存配置是否有效，如果配置无效则返回false
            if (!cac.isEnableCacheContext() && cac.getCachedAnnoConfig() == null &&
                    cac.getInvalidateAnnoConfigs() == null && cac.getUpdateAnnoConfig() == null) {
                cacheConfigMap.putByMethodInfo(key, CacheInvokeConfig.getNoCacheInvokeConfigInstance());
                return Boolean.FALSE;
            } else {
                // 将解析后的缓存配置存储，并返回true
                cacheConfigMap.putByMethodInfo(key, cac);
                return Boolean.TRUE;
            }
        }
    }


    /**
     * 根据目标类解析配置
     * 该方法递归地在目标类及其超类和接口中解析配置
     *
     * @param cac        缓存调用配置对象
     * @param clazz      目标类
     * @param name       方法名
     * @param paramTypes 参数类型数组
     */
    private void parseByTargetClass(CacheInvokeConfig cac, Class<?> clazz, String name, Class<?>[] paramTypes) {
        if (!clazz.isInterface() && clazz.getSuperclass() != null) {
            parseByTargetClass(cac, clazz.getSuperclass(), name, paramTypes);
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> it : interfaces) {
            parseByTargetClass(cac, it, name, paramTypes);
        }

        boolean matchThis = matchesThis(clazz);
        if (matchThis) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (methodMatch(name, method, paramTypes)) {
                    CacheConfigUtil.parse(cac, method);
                    break;
                }
            }
        }
    }

    /**
     * 检查方法是否匹配
     *
     * @param name       方法名
     * @param method     被检查的方法
     * @param paramTypes 参数类型数组
     * @return 如果方法匹配则返回true，否则返回false
     */
    private boolean methodMatch(String name, Method method, Class<?>[] paramTypes) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return Boolean.FALSE;
        }
        if (!name.equals(method.getName())) {
            return Boolean.FALSE;
        }
        Class<?>[] ps = method.getParameterTypes();
        if (ps.length != paramTypes.length) {
            return Boolean.FALSE;
        }
        for (int i = 0; i < ps.length; i++) {
            if (!ps[i].equals(paramTypes[i])) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 根据方法和目标类生成唯一键
     * 该方法用于根据方法和目标类的信息生成一个唯一的键值这个键值可以用于
     * 在缓存机制中唯一标识一个方法在特定类上的执行它通过拼接方法所属的类名、
     * 方法名和方法的描述符（返回类型和参数类型）来实现如果目标类非空还会拼接上目标类的名称
     * 这样可以确保即使在使用继承或覆盖方法时也能得到唯一的键值
     *
     * @param method      被调用的方法
     * @param targetClass 目标对象的类，可以是null
     * @return 唯一标识方法在特定类上执行的键值
     */
    public static String getKey(Method method, Class<?> targetClass) {
        StringBuilder sb = new StringBuilder();
        // 拼接方法所属的类名，确保键值能区分不同类中的同名方法
        sb.append(method.getDeclaringClass().getName());
        sb.append('.');
        // 拼接方法名，进一步细化键值的区分度
        sb.append(method.getName());
        // 通过方法的描述符（包括返回类型和参数类型）来增加键值的唯一性
        sb.append(Type.getMethodDescriptor(method));
        // 如果目标类非空，则拼接目标类的名称，用于处理静态方法或类方法的区分
        if (targetClass != null) {
            sb.append('_');
            sb.append(targetClass.getName());
        }
        // 返回拼接好的唯一键值字符串
        return sb.toString();
    }

}
