package com.yat.cache.anno.support;

import com.yat.cache.anno.method.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * ClassName DefaultCacheNameGenerator
 * <p>Description 默认的缓存名称生成器</p>
 *
 * @author Yat
 * Date 2024/8/22 22:04
 * version 1.0
 */
public class DefaultCacheNameGenerator implements CacheNameGenerator {

    private static final Map<Class<?>, Character> characterMap = Map.of(
            Integer.TYPE, 'I',
            Void.TYPE, 'V',
            Boolean.TYPE, 'Z',
            Byte.TYPE, 'B',
            Character.TYPE, 'C',
            Short.TYPE, 'S',
            Double.TYPE, 'D',
            Float.TYPE, 'F',
            Long.TYPE, 'J'
    );
    protected final String[] hiddenPackages;
    protected final ConcurrentHashMap<Method, String> cacheNameMap = new ConcurrentHashMap<>();

    public DefaultCacheNameGenerator(String[] hiddenPackages) {
        this.hiddenPackages = hiddenPackages;
    }

    @Override
    public String generateCacheName(Method method, Object targetObject) {
        String cacheName = cacheNameMap.get(method);

        if (cacheName == null) {
            final StringBuilder sb = new StringBuilder();

            String className = method.getDeclaringClass().getName();
            sb.append(ClassUtil.getShortClassName(removeHiddenPackage(hiddenPackages, className)));
            sb.append('.');
            sb.append(method.getName());
            sb.append('(');

            for (Class<?> c : method.getParameterTypes()) {
                getDescriptor(sb, c, hiddenPackages);
            }

            sb.append(')');

            String str = sb.toString();
            cacheNameMap.put(method, str);
            return str;
        }

        return cacheName;
    }

    @Override
    public String generateCacheName(Field field) {
        StringBuilder sb = new StringBuilder();
        String className = field.getDeclaringClass().getName();
        className = removeHiddenPackage(hiddenPackages, className);
        className = ClassUtil.getShortClassName(className);
        sb.append(className);
        sb.append(".").append(field.getName());
        return sb.toString();
    }

    @SuppressWarnings("PMD.AvoidPatternCompileInMethodRule")
    protected String removeHiddenPackage(String[] hiddenPackages, String packageOrFullClassName) {
        if (hiddenPackages != null && packageOrFullClassName != null) {
            for (String p : hiddenPackages) {
                if (p != null && packageOrFullClassName.startsWith(p)) {
                    packageOrFullClassName = Pattern.compile(p, Pattern.LITERAL).matcher(
                            packageOrFullClassName).replaceFirst("");
                    if (!packageOrFullClassName.isEmpty() && packageOrFullClassName.charAt(0) == '.') {
                        packageOrFullClassName = packageOrFullClassName.substring(1);
                    }
                    return packageOrFullClassName;
                }
            }
        }
        return packageOrFullClassName;
    }

    protected void getDescriptor(final StringBuilder sb, final Class<?> c, String[] hiddenPackages) {
        Class<?> d = c;

        while (true) {
            if (d.isPrimitive()) {
                char car = characterMap.getOrDefault(d, 'J');
                sb.append(car);
                return;
            } else if (d.isArray()) {
                sb.append('[');
                d = d.getComponentType();
            } else {
                sb.append('L');
                String name = d.getName();
                name = removeHiddenPackage(hiddenPackages, name);
                name = ClassUtil.getShortClassName(name);
                sb.append(name);
                sb.append(';');
                return;
            }
        }
    }
}
