// package com.yat.cache.autoconfigure;
//
// import com.yat.cache.core.CacheBuilder;
// import com.yat.cache.core.CacheConfigException;
// import com.yat.cache.core.external.ExternalCacheBuilder;
// import org.redisson.api.RedissonClient;
// import org.springframework.beans.BeansException;
// import org.springframework.context.ApplicationContext;
// import org.springframework.context.ApplicationContextAware;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Conditional;
// import org.springframework.context.annotation.Configuration;
//
// import java.util.Map;
// import java.util.Objects;
//
// /**
//  * Created on 2022/7/12.
//  *
//  * @author <a href="mailto:jeason1914@qq.com">yangyong</a>
//  */
// @Configuration
// @Conditional(RedissonAutoConfiguration.RedissonCondition.class)
// public class RedissonAutoConfiguration {
//     private static final String CACHE_TYPE = "redisson";
//
//     @Bean
//     public RedissonAutoInit redissonAutoInit() {
//         return new RedissonAutoInit();
//     }
//
//     public static class RedissonCondition extends JetCacheCondition {
//         public RedissonCondition() {
//             super(CACHE_TYPE);
//         }
//     }
//
//     public static class RedissonAutoInit extends ExternalCacheAutoInit implements ApplicationContextAware {
//         private ApplicationContext context;
//
//         public RedissonAutoInit() {
//             super(CACHE_TYPE);
//         }
//
//         @Override
//         protected CacheBuilder initCache(final ConfigTree ct, final String cacheAreaWithPrefix) {
//             final Map<String, RedissonClient> beans = this.context.getBeansOfType(RedissonClient.class);
//             if (beans.isEmpty()) {
//                 throw new CacheConfigException("no RedissonClient in spring context");
//             }
//             RedissonClient client = beans.values().iterator().next();
//             if (beans.size() > 1) {
//                 final String redissonClientName = ct.getProperty("redissonClient");
//                 if (Objects.isNull(redissonClientName) || redissonClientName.isEmpty()) {
//                     throw new CacheConfigException("redissonClient is required, because there is multiple RedissonClient in Spring context");
//                 }
//                 if (!beans.containsKey(redissonClientName)) {
//                     throw new CacheConfigException("there is no RedissonClient named " + redissonClientName + " in Spring context");
//                 }
//                 client = beans.get(redissonClientName);
//             }
//             final ExternalCacheBuilder<?> builder = RedissonCacheBuilder.createBuilder().redissonClient(client);
//             parseGeneralConfig(builder, ct);
//             return builder;
//         }
//
//         @Override
//         public void setApplicationContext(final ApplicationContext context) throws BeansException {
//             this.context = context;
//         }
//     }
// }
