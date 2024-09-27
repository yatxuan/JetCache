package com.yat.cache.autoconfigure.properties.enums;

/**
 * ClassName ReadFromEnums
 * Description 读取策略
 *
 * @author Yat
 * Date 2024/9/24 14:14
 * version 1.0
 */
public enum ReadFromEnum {
    /**
     * 从主库读取
     */
    MASTER,
    /**
     * 从主库读取，如果主库不可用，则从从库读取
     */
    MASTER_PREFERRED,
    /**
     * 从上游读取
     */
    UPSTREAM,

    /**
     * 从上游读取，如果上游不可用，则从主库读取
     */
    UPSTREAM_PREFERRED,

    /**
     * 首先从副本节点读取数据，只有在所有副本节点都无法读取时才从主节点（master）读取
     */
    REPLICA_PREFERRED,

    /**
     * 从从库读取
     */
    REPLICA,
    /**
     * Setting to read from the node with the lowest latency during topology discovery. Note that latency
     * measurements are
     * momentary snapshots that can change in rapid succession. Requires dynamic refresh sources to obtain topologies
     * and
     * latencies from all nodes in the cluster.
     *
     * @since 6.1.7
     */
    LOWEST_LATENCY,
    /**
     * 设置为从任何节点读取。
     *
     * @since 5.2
     */
    ANY,

    /**
     * 设置为从任何副本节点读取
     *
     * @since 6.0.1
     */
    ANY_REPLICA,


}
