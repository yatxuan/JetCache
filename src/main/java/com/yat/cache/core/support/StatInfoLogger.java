package com.yat.cache.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * StatInfo的日志记录器类，实现了Consumer<StatInfo>接口，用于消费并记录StatInfo对象中的缓存统计信息。
 * 该类通过日志形式输出缓存的统计信息，支持详细模式和摘要模式的日志输出。
 *
 * @author Yat
 * Date 2024/8/22 20:14
 * version 1.0
 */
public class StatInfoLogger implements Consumer<StatInfo> {

    /**
     * 使用SLF4J日志框架记录信息
     */
    private static final Logger logger = LoggerFactory.getLogger(StatInfoLogger.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    /**
     * 控制是否使用详细日志模式
     */
    private final boolean verboseLog;
    /**
     * 保护缓存名称的最大长度，超过该长度将被截断
     */
    protected int maxNameLength = 65;

    /**
     * 初始化日志模式。
     *
     * @param verboseLog 是否使用详细日志模式
     */
    public StatInfoLogger(boolean verboseLog) {
        this.verboseLog = verboseLog;
    }

    /**
     * 接受并处理StatInfo对象，主要用于排序缓存统计信息、生成日志并记录。
     * 首先根据缓存名称对统计信息进行排序，然后根据日志模式选择详细或摘要日志生成方式，
     * 最终记录日志信息。
     *
     * @param statInfo 缓存统计信息
     */
    @Override
    public void accept(StatInfo statInfo) {
        List<CacheStat> stats = statInfo.getStats();
        // 根据缓存名称排序统计信息，null名称的缓存排在前面
        stats.sort(StatInfoLogger::sort);
        StringBuilder sb;
        // 根据配置选择日志模式
        if (verboseLog) {
            sb = logVerbose(statInfo);
        } else {
            sb = logStatSummary(statInfo);
        }
        logger.info(sb.toString());
    }

    /**
     * Description: 根据缓存名称排序统计信息，null名称的缓存排在前面
     * <p>
     * Date: 2024/8/25 下午3:08
     */
    private static int sort(CacheStat o1, CacheStat o2) {
        if (o1.getCacheName() == null) {
            return -1;
        } else if (o2.getCacheName() == null) {
            return 1;
        } else {
            return o1.getCacheName().compareTo(o2.getCacheName());
        }
    }

    private StringBuilder logVerbose(StatInfo statInfo) {
        StringBuilder sb = logTitle(8192, statInfo);
        List<CacheStat> stats = statInfo.getStats();

        for (CacheStat s : stats) {
            String title = String.format(
                    "%-10s|%10s|%14s|%14s|%14s|%14s|%14s|%9s|%7s|%7s", "oper", "qps/tps", "count",
                    "success/hit", "fail", "miss", "expired", "avgTime", "minTime", "maxTime"
            );

            printSepLine(sb, title);

            sb.append(s.getCacheName())
                    .append("(hit rate ")
                    .append(String.format("%.3f", s.hitRate() * 100))
                    .append("%)").append('\n')
                    .append(title).append('\n');

            printSepLine(sb, title);

            sb.append(String.format("%-10s", "get")).append('|');
            sb.append(String.format("%,10.2f", s.qps())).append('|');
            sb.append(String.format("%,14d", s.getGetCount())).append('|');
            sb.append(String.format("%,14d", s.getGetHitCount())).append('|');
            sb.append(String.format("%,14d", s.getGetFailCount())).append('|');
            sb.append(String.format("%,14d", s.getGetMissCount())).append('|');
            sb.append(String.format("%,14d", s.getGetExpireCount())).append('|');
            sb.append(String.format("%,9.1f", s.avgGetTime())).append('|');
            sb.append(String.format("%,7d", s.getMinGetTime() == Long.MAX_VALUE ? 0 : s.getMinGetTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxGetTime())).append('\n');

            sb.append(String.format("%-10s", "put")).append('|');
            sb.append(String.format("%,10.2f", s.putTps())).append('|');
            sb.append(String.format("%,14d", s.getPutCount())).append('|');
            sb.append(String.format("%,14d", s.getPutSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getPutFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgPutTime())).append('|');
            sb.append(String.format("%,7d", s.getMinPutTime() == Long.MAX_VALUE ? 0 : s.getMinPutTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxPutTime())).append('\n');

            sb.append(String.format("%-10s", "remove")).append('|');
            sb.append(String.format("%,10.2f", s.removeTps())).append('|');
            sb.append(String.format("%,14d", s.getRemoveCount())).append('|');
            sb.append(String.format("%,14d", s.getRemoveSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getRemoveFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgRemoveTime())).append('|');
            sb.append(String.format("%,7d", s.getMinRemoveTime() == Long.MAX_VALUE ? 0 : s.getMinRemoveTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxRemoveTime())).append('\n');

            sb.append(String.format("%-10s", "load")).append('|');
            sb.append(String.format("%,10.2f", s.loadQps())).append('|');
            sb.append(String.format("%,14d", s.getLoadCount())).append('|');
            sb.append(String.format("%,14d", s.getLoadSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getLoadFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgLoadTime())).append('|');
            sb.append(String.format("%,7d", s.getMinLoadTime() == Long.MAX_VALUE ? 0 : s.getMinLoadTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxLoadTime())).append('\n');

        }
        return sb;
    }

    /**
     * 生成详细模式的日志信息。
     *
     * @param statInfo 缓存统计信息
     * @return 详细模式的日志内容
     */
    private StringBuilder logStatSummary(StatInfo statInfo) {
        StringBuilder sb = logTitle(2048, statInfo);

        List<CacheStat> stats = statInfo.getStats();
        // 计算缓存名称列的最大长度
        OptionalInt maxCacheNameLength = stats.stream()
                .mapToInt((s) -> getName(s.getCacheName()).length())
                .max();
        int len = Math.max(5, maxCacheNameLength.orElse(0));

        // 格式化表头
        String title = String.format(
                "%-" + len + "s|%10s|%7s|%14s|%14s|%14s|%14s|%11s|%11s", "cache", "qps", "rate",
                "get", "hit", "fail", "expire", "avgLoadTime", "maxLoadTime"
        );
        sb.append(title).append('\n');
        printSepLine(sb, title);
        // 格式化并追加每个缓存的统计信息
        for (CacheStat s : stats) {
            sb.append(String.format("%-" + len + "s", getName(s.getCacheName()))).append('|');
            sb.append(String.format("%,10.2f", s.qps())).append('|');
            sb.append(String.format("%6.2f%%", s.hitRate() * 100)).append('|');
            sb.append(String.format("%,14d", s.getGetCount())).append('|');
            sb.append(String.format("%,14d", s.getGetHitCount())).append('|');
            sb.append(String.format("%,14d", s.getGetFailCount())).append('|');
            sb.append(String.format("%,14d", s.getGetExpireCount())).append('|');
            sb.append(String.format("%,11.1f", s.avgLoadTime())).append('|');
            sb.append(String.format("%,11d", s.getMaxLoadTime())).append('\n');
        }
        printSepLine(sb, title);
        return sb;
    }

    /**
     * 生成标题行，包括时间范围。
     *
     * @param initSize 初始化StringBuilder的大小
     * @param statInfo 统计信息，用于获取时间范围
     * @return 标题行的StringBuilder对象
     */
    private StringBuilder logTitle(int initSize, StatInfo statInfo) {
        return new StringBuilder(initSize).append("JetCache stat from ")
                .append(SDF.format(new Date(statInfo.getStartTime())))
                .append(" to ")
                .append(SDF.format(statInfo.getEndTime()))
                .append("\n");
    }

    /**
     * 打印分隔线，对应标题或内容行的格式。
     *
     * @param sb    StringBuilder对象，用于组装日志内容
     * @param title 标题内容，用于生成分隔线
     */
    private void printSepLine(StringBuilder sb, String title) {
        title.chars().forEach((c) -> {
            if (c == '|') {
                sb.append('+');
            } else {
                sb.append('-');
            }
        });
        sb.append('\n');
    }


    /**
     * 获取或截断缓存名称，确保不超过最大长度。
     *
     * @param name 缓存名称
     * @return 截断后的缓存名称
     */
    private String getName(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() > maxNameLength) {
            return "..." + name.substring(name.length() - maxNameLength + 3);
        } else {
            return name;
        }
    }
}
