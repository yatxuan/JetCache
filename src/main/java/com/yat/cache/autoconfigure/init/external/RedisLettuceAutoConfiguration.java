package com.yat.cache.autoconfigure.init.external;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.RemoteCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.ReadFromEnum;
import com.yat.cache.autoconfigure.properties.enums.RedisModeEnum;
import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
import com.yat.cache.autoconfigure.properties.redis.RedisPropertiesConfig;
import com.yat.cache.autoconfigure.properties.redis.lettuce.ClusterPropertiesConfig;
import com.yat.cache.autoconfigure.properties.redis.lettuce.SentinelPropertiesConfig;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.redis.lettuce.JetCacheCodec;
import com.yat.cache.redis.lettuce.LettuceConnectionManager;
import com.yat.cache.redis.lettuce.RedisLettuceCacheBuilder;
import com.yat.cache.redis.lettuce.RedisLettuceCacheConfig;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName RedisLettuceAutoConfiguration
 * <p>Description RedisLettuce自动装配</p>
 *
 * @author Yat
 * Date 2024/9/25 09:54
 * version 1.0
 */
@Configuration
@SuppressWarnings({"unchecked", "rawtypes"})
@Conditional(RedisLettuceAutoConfiguration.RedisLettuceCondition.class)
public class RedisLettuceAutoConfiguration {

    public static final String AUTO_INIT_BEAN_NAME = "redisLettuceAutoInit";

    @Bean(name = {AUTO_INIT_BEAN_NAME})
    public RedisLettuceAutoInit redisLettuceAutoInit() {
        return new RedisLettuceAutoInit();
    }


    public static class RedisLettuceCondition extends JetCacheCondition {
        public RedisLettuceCondition() {
            super(RemoteCacheTypeEnum.REDIS_LETTUCE.getUpperName());
        }
    }

    public static class RedisLettuceAutoInit extends ExternalCacheAutoInit {

        public RedisLettuceAutoInit() {
            super(RemoteCacheTypeEnum.REDIS_LETTUCE.getUpperName());
        }

        @Override
        protected ExternalCacheBuilder<?> createExternalCacheBuilder(
                RemoteCacheProperties cacheProperties, String cacheAreaWithPrefix
        ) {
            RedisModeEnum mode = cacheProperties.getLettuce().getMode();
            boolean enablePubSub = parseBroadcastChannel(cacheProperties) != null;

            Long asyncResultTimeoutInMillis = cacheProperties.getLettuce().getAsyncResultTimeoutInMillis();
            if (asyncResultTimeoutInMillis == null) {
                asyncResultTimeoutInMillis = DefaultCacheConstant.ASYNC_RESULT_TIMEOUT.toMillis();
            }

            ExternalCacheBuilder<?> externalCacheBuilder;

            if (Objects.requireNonNull(mode) == RedisModeEnum.CLUSTER) {
                externalCacheBuilder = createCluster(
                        cacheProperties, enablePubSub, asyncResultTimeoutInMillis
                );
            } else if (mode == RedisModeEnum.SENTINEL) {
                externalCacheBuilder = createSentinel(
                        cacheProperties, enablePubSub, asyncResultTimeoutInMillis
                );
            } else if (mode == RedisModeEnum.SINGLETON) {
                externalCacheBuilder = createStandalone(
                        cacheProperties.getLettuce().getSingleton(), enablePubSub, asyncResultTimeoutInMillis
                );
            } else {
                throw new IllegalArgumentException("unknown mode:" + mode);
            }
            return externalCacheBuilder;
        }

        @Override
        protected void afterExternalCacheInit(
                ExternalCacheBuilder<?> builder, RemoteCacheProperties cacheProperties, String cacheAreaWithPrefix
        ) {
            setCustomContainer(cacheAreaWithPrefix, builder);
        }

        /**
         * 为自定义缓存容器设置Redis客户端和连接等信息
         *
         * @param cacheAreaWithPrefix  缓存区域的前缀，用于在自定义容器中标识不同的缓存配置
         * @param externalCacheBuilder 外部缓存构建器，用于获取Redis配置信息
         */
        private void setCustomContainer(String cacheAreaWithPrefix, ExternalCacheBuilder<?> externalCacheBuilder) {
            // 获取Redis缓存配置
            RedisLettuceCacheConfig config = (RedisLettuceCacheConfig) externalCacheBuilder.getConfig();
            // 获取Redis客户端
            AbstractRedisClient client = config.getRedisClient();
            // 获取Redis连接
            StatefulConnection<byte[], byte[]> connection = config.getConnection();
            // 获取Spring Boot自动配置的自定义容器
            Map<String, Object> customContainer = autoConfigureBeans.getCustomContainer();

            // 向自定义容器中添加Redis客户端
            customContainer.put(cacheAreaWithPrefix + ".client", client);
            // 创建并初始化连接管理器
            LettuceConnectionManager m = LettuceConnectionManager.defaultManager();
            m.init(client, connection);
            // 向自定义容器中添加Redis连接
            customContainer.put(cacheAreaWithPrefix + ".connection", m.connection(client));
            // 向自定义容器中添加Redis命令
            customContainer.put(cacheAreaWithPrefix + ".commands", m.commands(client));
            // 向自定义容器中添加异步Redis命令
            customContainer.put(cacheAreaWithPrefix + ".asyncCommands", m.asyncCommands(client));
            // 向自定义容器中添加响应式Redis命令
            customContainer.put(cacheAreaWithPrefix + ".reactiveCommands", m.reactiveCommands(client));
        }

        private ExternalCacheBuilder<?> createCluster(
                RemoteCacheProperties remoteCacheProperties, boolean enablePubSub, Long asyncResultTimeoutInMillis
        ) {
            StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection = null;
            ClusterPropertiesConfig cluster = remoteCacheProperties.getLettuce().getCluster();

            ReadFromEnum readFromEnum = remoteCacheProperties.getLettuce().getReadFrom();
            ReadFrom readFrom = null;
            if (readFromEnum != null) {
                readFrom = ReadFrom.valueOf(readFromEnum.name().trim());
            }

            List<RedisURI> redisURIList = cluster.getNodes().stream()
                    .map(this::createRedisURI)
                    .toList();
            RedisClusterClient client = RedisClusterClient.create(redisURIList);
            StatefulConnection<byte[], byte[]> connection = clusterConnection(
                    remoteCacheProperties, readFrom, client, false
            );
            if (enablePubSub) {
                pubSubConnection = (StatefulRedisPubSubConnection) clusterConnection(
                        remoteCacheProperties, readFrom, client, true
                );
            }

            return RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                    .connection(connection)
                    .pubSubConnection(pubSubConnection)
                    .redisClient(client)
                    .asyncResultTimeoutInMillis(asyncResultTimeoutInMillis);
        }

        private ExternalCacheBuilder<?> createSentinel(
                RemoteCacheProperties remoteCacheProperties, boolean enablePubSub, Long asyncResultTimeoutInMillis
        ) {
            SentinelPropertiesConfig sentinel = remoteCacheProperties.getLettuce().getSentinel();
            RedisPropertiesConfig master = sentinel.getMaster();

            ReadFromEnum readFromEnum = remoteCacheProperties.getLettuce().getReadFrom();
            ReadFrom readFrom = null;
            if (readFromEnum != null) {
                readFrom = ReadFrom.valueOf(readFromEnum.name().trim());
            }

            RedisURI.Builder builder = RedisURI.Builder
                    .sentinel(master.getHost(), master.getPort(), sentinel.getMasterName())
                    .withPassword(master.getPassword().toCharArray());
            for (RedisPropertiesConfig slave : sentinel.getSlave()) {
                builder.withSentinel(slave.getHost(), slave.getPort(), slave.getPassword());
            }
            RedisURI redisUri = builder.build();

            RedisClient client = RedisClient.create();
            client.setOptions(
                    ClientOptions.builder().
                            disconnectedBehavior(
                                    ClientOptions.DisconnectedBehavior.REJECT_COMMANDS
                            ).build()
            );

            StatefulRedisMasterReplicaConnection connection = MasterReplica.connect(
                    client, new JetCacheCodec(), redisUri
            );
            if (readFrom != null) {
                connection.setReadFrom(readFrom);
            }

            RedisLettuceCacheBuilder.RedisLettuceCacheBuilderImpl redisLettuceCacheBuilder =
                    RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                            .connection(connection)
                            .redisClient(client)
                            .asyncResultTimeoutInMillis(asyncResultTimeoutInMillis);
            if (enablePubSub) {
                StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection =
                        client.connectPubSub(new JetCacheCodec());
                redisLettuceCacheBuilder.pubSubConnection(pubSubConnection);
            }
            return redisLettuceCacheBuilder;
        }

        private ExternalCacheBuilder<?> createStandalone(
                RedisPropertiesConfig singleton, boolean enablePubSub, Long asyncResultTimeoutInMillis
        ) {
            StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection = null;

            RedisURI redisURI = createRedisURI(singleton);
            RedisClient client = RedisClient.create(redisURI);
            client.setOptions(
                    ClientOptions.builder().
                            disconnectedBehavior(
                                    ClientOptions.DisconnectedBehavior.REJECT_COMMANDS
                            ).build()
            );

            StatefulRedisMasterReplicaConnection<byte[], byte[]> connection = MasterReplica.connect(
                    client, new JetCacheCodec(), redisURI
            );

            if (enablePubSub) {
                pubSubConnection = client.connectPubSub(new JetCacheCodec(), redisURI);
            }

            return RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                    .connection(connection)
                    .pubSubConnection(pubSubConnection)
                    .redisClient(client)
                    .asyncResultTimeoutInMillis(asyncResultTimeoutInMillis);
        }

        private RedisURI createRedisURI(RedisPropertiesConfig redisProperties) {
            RedisURI redisURI;

            String url = redisProperties.getUrl();
            if (Objects.nonNull(url) && !url.isBlank()) {
                redisURI = RedisURI.create(url);
            } else {
                redisURI = RedisURI.create(redisProperties.getHost(), redisProperties.getPort());
                String password = redisProperties.getPassword();
                if (Objects.nonNull(password) && !password.isBlank()) {
                    redisURI.setPassword(password.toCharArray());
                }
            }

            redisURI.setDatabase(redisProperties.getDatabase());
            return redisURI;
        }

        /**
         * 根据配置建立集群连接
         *
         * @param remoteCacheProperties 配置树，用于获取连接配置
         * @param readFrom              指定读取数据的节点类型，可以在主节点或从节点读取
         * @param client                Redis集群客户端
         * @param pubsub                是否建立发布/订阅连接
         * @return 返回建立的连接实例
         */
        private StatefulConnection<byte[], byte[]> clusterConnection(
                RemoteCacheProperties remoteCacheProperties, ReadFrom readFrom,
                RedisClusterClient client, boolean pubsub
        ) {
            // 获取周期性刷新使能的间隔时间，默认为60秒
            Integer enablePeriodicRefresh = remoteCacheProperties.getLettuce().getEnablePeriodicRefresh();
            if (Objects.isNull(enablePeriodicRefresh)) {
                enablePeriodicRefresh = 60;
            }
            // 获取是否启用所有自适应刷新触发器的标志，默认为true
            Boolean enableAllAdaptiveRefreshTriggers =
                    remoteCacheProperties.getLettuce().getEnableAllAdaptiveRefreshTriggers();
            if (ObjectUtils.isEmpty(enablePeriodicRefresh)) {
                enableAllAdaptiveRefreshTriggers = true;
            }

            // 构建集群拓扑刷新选项
            ClusterTopologyRefreshOptions.Builder topologyOptionBuilder = ClusterTopologyRefreshOptions.builder();
            // 如果周期性刷新使能间隔大于0，则启用周期性刷新
            if (enablePeriodicRefresh > 0) {
                topologyOptionBuilder.enablePeriodicRefresh(Duration.ofSeconds(enablePeriodicRefresh));
            }
            // 如果启用了所有自适应刷新触发器，则进行相应配置
            if (enableAllAdaptiveRefreshTriggers) {
                topologyOptionBuilder.enableAllAdaptiveRefreshTriggers();
            }

            // 构建集群客户端选项，并配置拓扑刷新选项和断开连接时的行为
            ClusterClientOptions options = ClusterClientOptions.builder()
                    .topologyRefreshOptions(topologyOptionBuilder.build())
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .build();
            // 设置客户端选项
            client.setOptions(options);
            // 根据是否为发布/订阅连接，选择不同的连接方法
            if (pubsub) {
                // 对于发布/订阅连接
                return client.connectPubSub(new JetCacheCodec());
            } else {
                // 对于普通连接
                StatefulRedisClusterConnection<byte[], byte[]> c = client.connect(new JetCacheCodec());
                // 如果读取策略不为空，则设置读取策略
                if (readFrom != null) {
                    c.setReadFrom(readFrom);
                }
                return c;
            }
        }

    }
}
