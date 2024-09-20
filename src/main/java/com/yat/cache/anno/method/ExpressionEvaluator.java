package com.yat.cache.anno.method;

import com.yat.cache.core.CacheConfigException;
import org.mvel2.MVEL;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类名 ExpressionEvaluator
 * <p>描述 该类实现了Function接口，用于解析和执行表达式</p>
 * <p>
 * 它可以根据不同的表达式语言（如SPRING_EL，MVEL）创建相应的解析器。
 * <T> 泛型参数，表示输入类型
 * <R> 泛型参数，表示输出类型
 *
 * @author Yat
 * Date 2024/8/22 21:59
 * version 1.0
 */
public class ExpressionEvaluator implements Function<Object, Object> {
    /**
     * 定义一个正则表达式模式，用于解析表达式字符串。
     */
    private static final Pattern pattern = Pattern.compile("\\s*(\\w+)\\s*\\{(.+)\\}\\s*");
    /**
     * 目标函数，用于实际执行表达式解析和计算。
     */
    private Function<Object, Object> target;

    /**
     * 构造函数，用于初始化ExpressionEvaluator。
     *
     * @param script       表达式字符串
     * @param defineMethod 定义方法
     */
    public ExpressionEvaluator(String script, Method defineMethod) {
        Object[] rt = parseEL(script);
        EL el = (EL) rt[0];
        String realScript = (String) rt[1];
        if (el == EL.MVEL) {
            target = new MvelEvaluator(realScript);
        } else if (el == EL.SPRING_EL) {
            target = new SpelEvaluator(realScript, defineMethod);
        }
    }

    /**
     * 解析表达式语言并返回解析结果。
     *
     * @param script 要解析的表达式字符串
     * @return 包含解析结果的数组
     */
    private Object[] parseEL(String script) {
        if (script == null || script.trim().isEmpty()) {
            return null;
        }
        Object[] rt = new Object[2];
        Matcher matcher = pattern.matcher(script);
        if (!matcher.matches()) {
            rt[0] = EL.SPRING_EL; // default spel since 2.4
            rt[1] = script;
        } else {
            String s = matcher.group(1);
            if ("spel".equals(s)) {
                rt[0] = EL.SPRING_EL;
            } else if ("mvel".equals(s)) {
                rt[0] = EL.MVEL;
            }/* else if ("buildin".equals(s)) {
                rt[0] = EL.BUILD_IN;
            } */ else {
                throw new CacheConfigException("Can't parse \"" + script + "\"");
            }
            rt[1] = matcher.group(2);
        }
        return rt;
    }

    /**
     * 覆盖apply方法，应用给定的函数到参数上
     * <p>
     * 本方法的主要作用是接受一个对象作为参数，并将其传递给target对象的apply方法进行处理
     * 它作为函数式编程的一部分，用于执行函数合成或委托调用，不改变原始函数的行为
     *
     * @param o 要应用的函数的参数，可以是任意类型
     * @return 返回target对象apply方法调用的结果，结果类型依赖于具体函数的实现
     */
    @Override
    public Object apply(Object o) {
        return target.apply(o);
    }

    /**
     * 获取内部的目标函数。
     *
     * @return 目标函数
     */
    Function<Object, Object> getTarget() {
        return target;
    }
}

/**
 * MvelEvaluator类实现了Function接口，用于在给定上下文环境中评估MVEL脚本
 * 它提供了一种方式来动态执行MVEL脚本，并根据输入的上下文返回评估结果
 */
class MvelEvaluator implements Function<Object, Object> {
    /**
     * 存储待评估的MVEL脚本
     */
    private final String script;

    /**
     * 用于初始化MvelEvaluator对象
     *
     * @param script 要评估的MVEL脚本，它定义了在特定上下文中执行的逻辑
     */
    public MvelEvaluator(String script) {
        this.script = script;
    }

    /**
     * 使用给定的上下文评估MVEL脚本
     *
     * @param context 一个包含评估上下文的对象，允许脚本访问对象的属性和方法
     * @return 脚本评估的结果，类型取决于脚本的执行结果
     */
    @Override
    public Object apply(Object context) {
        // 使用MVEL.eval方法执行脚本并返回结果
        return MVEL.eval(script, context);
    }
}

/**
 * SpelEvaluator类实现了Function接口，用于解析和执行Spring Expression Language(SpEL)表达式。
 * 该类主要用于在给定的根对象和方法定义上下文中评估SpEL表达式。
 */
class SpelEvaluator implements Function<Object, Object> {

    /**
     * 表达式解析器，用于解析SpEL表达式。
     */
    private static final ExpressionParser parser;

    /**
     * 参数名发现器，用于获取方法参数名。
     */
    private static final ParameterNameDiscoverer parameterNameDiscoverer;

    /*
     * 静态初始化块，初始化表达式解析器和参数名发现器。
     */
    static {
        parser = new SpelExpressionParser();
        parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    }

    /**
     * 表达式对象，保存解析后的SpEL表达式。
     */
    private final Expression expression;

    /**
     * 参数名数组，保存方法的参数名。
     */
    private String[] parameterNames;

    /**
     * 构造函数，用于初始化SpEL表达式解析器和参数名数组。
     *
     * @param script       SpEL表达式的脚本。
     * @param defineMethod 定义的Java方法，用于获取参数名。
     */
    public SpelEvaluator(String script, Method defineMethod) {
        expression = parser.parseExpression(script);
        if (defineMethod.getParameterCount() > 0) {
            parameterNames = parameterNameDiscoverer.getParameterNames(defineMethod);
        }
    }
    /**
     * 应用函数，使用给定的根对象评估SpEL表达式。
     *
     * @param rootObject 根对象，用于评估上下文。
     * @return 表达式评估后的结果。
     */
    @Override
    public Object apply(Object rootObject) {
        // 创建评估上下文，设置根对象
        EvaluationContext context = new StandardEvaluationContext(rootObject);
        // 将根对象转换为缓存调用上下文
        CacheInvokeContext cic = (CacheInvokeContext) rootObject;
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], cic.getArgs()[i]);
            }
        }
        // 将结果变量设置为缓存调用上下文的结果
        context.setVariable("result", cic.getResult());

        // 使用评估上下文执行SpEL表达式并返回结果
        return expression.getValue(context);
    }
}
