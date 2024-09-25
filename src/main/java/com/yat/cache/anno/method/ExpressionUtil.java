package com.yat.cache.anno.method;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.support.CacheAnnoConfig;
import com.yat.cache.anno.support.CacheUpdateAnnoConfig;
import com.yat.cache.anno.support.CachedAnnoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassName ExpressionUtil
 * <p>Description 表达式评估工具类</p>
 * 该类提供了评估缓存注解中条件表达式、后置条件表达式、键表达式和值表达式的工具方法。
 * 它们根据注解配置和调用上下文动态计算缓存操作的条件、键和值。
 *
 * @author Yat
 * Date 2024/8/22 22:00
 * version 1.0
 */
class ExpressionUtil {

    /**
     * 评估失败时返回的特殊对象
     */
    static Object EVAL_FAILED = new Object();
    private static final Logger logger = LoggerFactory.getLogger(ExpressionUtil.class);

    /**
     * 评估缓存操作的前置条件是否满足。
     *
     * @param context 缓存调用上下文，包含了方法信息和参数
     * @param cac     缓存注解配置对象，包含了缓存操作的配置信息
     * @return 如果条件满足返回true，否则返回false。如果评估过程中发生异常也返回false。
     */
    public static boolean evalCondition(CacheInvokeContext context, CacheAnnoConfig cac) {
        String condition = cac.getCondition();
        try {
            if (cac.getConditionEvaluator() == null) {
                if (DefaultCacheConstant.isUndefined(condition)) {
                    // 如果未定义条件，则默认条件始终为真
                    cac.setConditionEvaluator(o -> true);
                } else {
                    // 创建表达式评估器并评估条件
                    ExpressionEvaluator e = new ExpressionEvaluator(condition, cac.getDefineMethod());
                    cac.setConditionEvaluator((o) -> (Boolean) e.apply(o));
                }
            }
            return cac.getConditionEvaluator().apply(context);
        } catch (Exception e) {
            // logger.error("评估前置条件'{}' 出错, 方法:{}:{}", condition, context.getMethod(), e.getMessage(), e);
            logger.error(
                    "error occurs when eval condition '{}' in {}:{}",
                    condition, context.getMethod(), e.getMessage(), e
            );
            return false;
        }
    }

    /**
     * 评估缓存操作的后置条件是否满足。
     *
     * @param context 缓存调用上下文，包含了方法信息和参数
     * @param cac     缓存注解配置对象，特别地，这是用于缓存存储的配置
     * @return 如果条件满足返回true，否则返回false。如果评估过程中发生异常也返回false。
     */
    public static boolean evalPostCondition(CacheInvokeContext context, CachedAnnoConfig cac) {
        String postCondition = cac.getPostCondition();
        try {
            if (cac.getPostConditionEvaluator() == null) {
                if (DefaultCacheConstant.isUndefined(postCondition)) {
                    // 如果未定义后置条件，则默认条件始终为真
                    cac.setPostConditionEvaluator(o -> true);
                } else {
                    // 创建表达式评估器并评估后置条件
                    ExpressionEvaluator e = new ExpressionEvaluator(postCondition, cac.getDefineMethod());
                    cac.setPostConditionEvaluator((o) -> (Boolean) e.apply(o));
                }
            }
            return cac.getPostConditionEvaluator().apply(context);
        } catch (Exception e) {
            // logger.error("评估后置条件 \"{}\" 出错, 方法:{}:{}", postCondition, context.getMethod(), e.getMessage(), e);
            logger.error(
                    "error occurs when eval postCondition '{}' in {}:{}",
                    postCondition, context.getMethod(), e.getMessage(), e
            );
            return false;
        }
    }

    /**
     * 评估并生成缓存键。
     *
     * @param context 缓存调用上下文，包含了方法信息和参数
     * @param cac     缓存注解配置对象
     * @return 如果键评估成功则返回评估后的键，可以是单一值或复合值；如果评估失败返回null。
     */
    public static Object evalKey(CacheInvokeContext context, CacheAnnoConfig cac) {
        String keyScript = cac.getKey();
        try {
            if (cac.getKeyEvaluator() == null) {
                if (DefaultCacheConstant.isUndefined(keyScript)) {
                    // 如果未定义键脚本，则默认键评估器将使用方法的参数作为键
                    cac.setKeyEvaluator(o -> {
                        CacheInvokeContext c = (CacheInvokeContext) o;
                        return c.getArgs() == null || c.getArgs().length == 0 ? "_$JET_CACHE_NULL_KEY$_" : c.getArgs();
                    });
                } else {
                    // 创建表达式评估器并评估键
                    ExpressionEvaluator e = new ExpressionEvaluator(keyScript, cac.getDefineMethod());
                    cac.setKeyEvaluator(e::apply);
                }
            }
            return cac.getKeyEvaluator().apply(context);
        } catch (Exception e) {
            logger.error(
                    "error occurs when eval key \"{}\" in {}:{}",
                    keyScript, context.getMethod(), e.getMessage(), e
            );
            return null;
        }
    }

    /**
     * 评估并生成缓存更新的值。
     *
     * @param context 缓存调用上下文，包含了方法信息和参数
     * @param cac     缓存更新注解配置对象
     * @return 如果值评估成功则返回评估后的值；如果评估失败返回EVAL_FAILED对象。
     */
    public static Object evalValue(CacheInvokeContext context, CacheUpdateAnnoConfig cac) {
        String valueScript = cac.getValue();
        try {
            if (cac.getValueEvaluator() == null) {
                // 创建表达式评估器并评估值
                ExpressionEvaluator e = new ExpressionEvaluator(valueScript, cac.getDefineMethod());
                cac.setValueEvaluator(e::apply);
            }
            return cac.getValueEvaluator().apply(context);
        } catch (Exception e) {
            logger.error(
                    "error occurs when eval value \"{}\" in {}:{}",
                    valueScript, context.getMethod(), e.getMessage(), e
            );
            return EVAL_FAILED;
        }
    }
}
