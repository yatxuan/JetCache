package com.yat.cache.core.support;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ClassName StatInfo
 * <p>Description 缓存统计信息类</p>
 * 用于封装缓存统计的相关信息，包括缓存统计列表、统计开始时间和结束时间
 *
 * @author Yat
 * Date 2024/8/22 20:10
 * version 1.0
 */
@Setter
@Getter
public class StatInfo {
    /**
     * 缓存统计列表，包含多个缓存统计项
     */
    private List<CacheStat> stats;
    /**
     * 统计开始时间
     */
    private long startTime;
    /**
     * 统计结束时间
     */
    private long endTime;
}
