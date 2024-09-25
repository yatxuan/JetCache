package com.yat.cache.core.support;

import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ClassName SquashedLogger
 * <p>Description SquashedLogger类用于实现日志的间隔输出，避免在短时间内重复输出大量日志</p>
 * 它通过维护一个映射，确保每个Logger实例都有一个对应的SquashedLogger实例来控制日志输出频率。
 *
 * @author Yat
 * Date 2024/8/22 20:09
 * version 1.0
 */
public class SquashedLogger {
    /**
     * 默认日志输出间隔为10秒
     */
    private static final int DEFAULT_INTERVAL_SECONDS = 10;
    /**
     * 映射保存Logger实例与SquashedLogger实例的对应关系，用于单例式管理
     */
    private static final ConcurrentHashMap<Logger, SquashedLogger> MAP = new ConcurrentHashMap<>();
    /**
     * 日志记录器实例
     */
    private final Logger logger;

    /**
     * 日志输出间隔时间（纳秒）
     */
    private final long interval;

    /**
     * 记录上一次日志输出时间的原子长整型，用于线程安全地比较和设置时间
     */
    private final AtomicLong lastLogTime;

    private SquashedLogger(Logger logger, int intervalSeconds) {
        this.logger = logger;
        this.interval = Duration.ofSeconds(intervalSeconds).toNanos();
        this.lastLogTime = new AtomicLong(System.nanoTime() - interval);
    }

    /**
     * 输出错误日志，根据配置的间隔来控制日志输出频率。
     * 如果达到输出间隔，直接使用给定的消息和异常输出日志；
     * 否则，构建包含异常链信息的字符串并输出。
     *
     * @param msg 日志消息
     * @param e   异常
     */
    public void error(CharSequence msg, Throwable e) {
        if (shouldLogEx()) {
            logger.error(msg.toString(), e);
        } else {
            StringBuilder sb;
            if (msg instanceof StringBuilder) {
                sb = (StringBuilder) msg;
            } else {
                sb = new StringBuilder(msg.length() + 256);
                sb.append(msg);
            }
            sb.append(' ');
            int i = 0;
            while (e != null && i++ < 20) {
                sb.append(e);
                e = e.getCause();
                if (e != null) {
                    sb.append("\ncause by ");
                }
            }
            logger.error(sb.toString());
        }
    }

    /**
     * 检查是否应该记录日志，根据时间间隔判断。
     *
     * @return 如果应该记录日志，则返回true；否则返回false。
     */
    private boolean shouldLogEx() {
        long now = System.nanoTime();
        long last = lastLogTime.get();
        if (Math.abs(now - last) >= interval) {
            return lastLogTime.compareAndSet(last, now);
        } else {
            return false;
        }
    }

    /**
     * 获取或创建与给定Logger实例关联的SquashedLogger实例，使用默认的日志输出间隔。
     *
     * @param target 目标日志记录器实例
     * @return SquashedLogger实例
     */
    public static SquashedLogger getLogger(Logger target) {
        return getLogger(target, DEFAULT_INTERVAL_SECONDS);
    }

    /**
     * 获取或创建与给定Logger实例关联的SquashedLogger实例。
     *
     * @param target          目标日志记录器实例
     * @param intervalSeconds 日志输出间隔秒数
     * @return SquashedLogger实例
     */
    public static SquashedLogger getLogger(Logger target, int intervalSeconds) {
        SquashedLogger result = MAP.get(target);
        if (result == null) {
            result = MAP.computeIfAbsent(target, k -> new SquashedLogger(k, intervalSeconds));
        }
        return result;
    }
}
