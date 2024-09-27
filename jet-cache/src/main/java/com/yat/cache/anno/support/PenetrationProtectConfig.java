package com.yat.cache.anno.support;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * 类名 PenetrationProtectConfig
 * <p>描述 用于配置缓存穿透保护的相关参数</p>
 * <p>缓存穿透指的是查询的数据在缓存和数据库中都不存在，导致多次无效查询。</p>
 * <p>本配置类旨在通过设置保护超时时间等参数，减少缓存穿透带来的性能影响。</p>
 *
 * @author Yat
 * Date 2024/8/22 22:08
 * version 1.0
 */
@Setter
@Getter
public class PenetrationProtectConfig {

    /**
     * 是否开启缓存穿透保护
     */
    private boolean penetrationProtect;
    /**
     * 缓存穿透保护的超时时间
     * 在这段时间内，对同一数据的查询将被缓存，即使该数据在数据库中不存在
     */
    private Duration penetrationProtectTimeout;

}
