package com.yat.cache.autoconfigure.properties.redis;

import lombok.Data;

/**
 * ClassName RedisPropertiesConfig
 * Description redis配置
 *
 * @author Yat
 * Date 2024/9/24 11:02
 * version 1.0
 */
@Data
public class RedisPropertiesConfig {

    /**
     * Database index used by the connection factory.
     */
    private int database = 0;
    /**
     * Connection URL. Overrides host, port, and password. User is ignored. Example:
     * redis://user:password@example.com:6379
     */
    private String url;
    /**
     * Redis server host.
     */
    private String host = "localhost";
    /**
     * Login password of the redis server.
     */
    private String password;
    /**
     * Redis server port.
     */
    private int port = 6379;
}
