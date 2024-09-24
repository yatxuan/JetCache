package com.yat.cache.autoconfigure.properties.redis.lettuce;

import com.yat.cache.autoconfigure.properties.redis.RedisPropertiesConfig;
import lombok.Data;

import java.util.List;

/**
 * ClassName SentinelPropertiesConfig
 * Description 哨兵配置
 *
 * @author Yat
 * Date 2024/9/24 11:00
 * version 1.0
 */
@Data
public class SentinelPropertiesConfig {

    /**
     * 哨兵集群主节点的名称.
     */
    private String masterName;
    /**
     * 主节点的配置
     */
    private RedisPropertiesConfig master;
    /**
     * 从节点的配置
     */
    private List<RedisPropertiesConfig> slave;
}
