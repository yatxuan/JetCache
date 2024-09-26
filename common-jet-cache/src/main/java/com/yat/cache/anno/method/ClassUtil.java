package com.yat.cache.anno.method;

import org.springframework.asm.Type;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassUtil类提供了一组用于操作和处理类及其方法的工具函数
 */
public class ClassUtil {

    /**
     * 存储方法与其签名字符串的映射，以避免重复计算
     */
    private static final ConcurrentHashMap<Method, String> methodSigMap = new ConcurrentHashMap<>();

    /**
     * 将完整类名转换为简写形式，保留最后一个点后的部分作为类名
     *
     * @param className 完整的类名，带包路径
     * @return 简写形式的类名
     */
    public static String getShortClassName(String className) {
        if (className == null) {
            return null;
        }
        String[] ss = className.split("\\.");
        StringBuilder sb = new StringBuilder(className.length());
        for (int i = 0; i < ss.length; i++) {
            String s = ss[i];
            if (i != ss.length - 1) {
                sb.append(s.charAt(0)).append('.');
            } else {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    /**
     * 获取对象实现的所有接口，包括继承自超类的接口
     *
     * @param obj 需要检查的实例对象
     * @return 实现的所有接口的Class数组
     */
    public static Class<?>[] getAllInterfaces(Object obj) {
        Class<?> c = obj.getClass();
        HashSet<Class<?>> s = new HashSet<>();
        do {
            Class<?>[] its = c.getInterfaces();
            Collections.addAll(s, its);
            c = c.getSuperclass();
        } while (c != null);
        return s.toArray(new Class<?>[0]);
    }

    /**
     * 获取方法的签名，包括方法名和参数类型描述符
     *
     * @param m 反射得到的方法对象
     * @return 方法的签名字符串
     */
    public static String getMethodSig(Method m) {
        String sig = methodSigMap.get(m);
        if (sig == null) {
            StringBuilder sb = new StringBuilder();
            getMethodSig(sb, m);
            sig = sb.toString();
            methodSigMap.put(m, sig);
        }
        return sig;
    }

    /**
     * 获取方法的签名
     * <p>
     * 该方法的目的是通过Method对象获取方法名和描述符，从而构建方法的签名
     * 方法签名包括方法名和参数类型列表
     *
     * @param sb StringBuilder对象，用于拼接方法签名
     * @param m  Method对象，表示当前对象的方法
     */
    private static void getMethodSig(StringBuilder sb, Method m) {
        // 附方法名
        sb.append(m.getName());
        // 附加方法的描述符，包含了参数和返回值的类型信息
        sb.append(Type.getType(m).getDescriptor());
    }
}
