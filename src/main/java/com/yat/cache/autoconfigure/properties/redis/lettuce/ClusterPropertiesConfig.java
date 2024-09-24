package com.yat.cache.autoconfigure.properties.redis.lettuce;

import com.yat.cache.autoconfigure.properties.redis.RedisPropertiesConfig;
import lombok.Data;

import java.util.List;

/**
 * ClassName ClusterConfig
 * Description 集群: Cluster properties.
 *
 * @author Yat
 * Date 2024/9/24 11:04
 * version 1.0
 */
@Data
public class ClusterPropertiesConfig {

    /**
     * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
     * "initial" list of cluster nodes and is required to have at least one entry.
     */
    private List<RedisPropertiesConfig> nodes;

    /**
     * Maximum number of redirects to follow when executing commands across the
     * cluster.
     */
    private Integer maxRedirects;
}
