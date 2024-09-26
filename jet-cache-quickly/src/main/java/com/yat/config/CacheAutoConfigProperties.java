package com.yat.config;// package com.yat.config;
//
// import com.yat.cache.autoconfigure.properties.enums.KeyConvertorEnum;
// import com.yat.cache.autoconfigure.properties.enums.LocalCacheTypeEnum;
// import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
// import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
// import org.springframework.beans.factory.InitializingBean;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.env.ConfigurableEnvironment;
// import org.springframework.core.env.MapPropertySource;
// import org.springframework.core.env.MutablePropertySources;
//
// import java.util.HashMap;
// import java.util.Map;
//
// /**
//  * ClassName CacheAutoConfigProperties
//  * Description 通过 动态添加配置来启用缓存
//  *
//  * @author Yat
//  * Date 2024/9/26 15:30
//  * version 1.0
//  */
// public class CacheAutoConfigProperties implements InitializingBean {
//
//     private ConfigurableEnvironment environment;
//
//     @Override
//     public void afterPropertiesSet() {
//         init();
//     }
//
//     /**
//      * 动态添加配置
//      */
//     private void init() {
//         // 获取 MutablePropertySources
//         MutablePropertySources propertySources = environment.getPropertySources();
//
//         // 创建一个新的 MapPropertySource 并添加配置
//         MapPropertySource propertySource = new MapPropertySource("jet-cache", createPropertiesMap());
//
//         // 将新的配置添加到 propertySources
//         propertySources.addLast(propertySource);
//
//     }
//
//     private static Map<String, Object> createPropertiesMap() {
//
//         Map<String, Object> localCacheDefault = new HashMap<>();
//         localCacheDefault.put("type", LocalCacheTypeEnum.LINKED_HASH_MAP);
//         localCacheDefault.put("key-convertor", KeyConvertorEnum.GSON);
//
//         Map<String, Object> remoteCacheDefault = new HashMap<>();
//         remoteCacheDefault.put("type", RemoteCacheTypeEnum.REDIS_SPRING_DATA);
//         remoteCacheDefault.put("key-convertor", KeyConvertorEnum.GSON);
//         remoteCacheDefault.put("broadcast-channel", "broadcast-channel-data");
//         remoteCacheDefault.put("key-prefix", "default:");
//         remoteCacheDefault.put("value-encoder", SerialPolicyTypeEnum.KRYO5);
//         remoteCacheDefault.put("value-decoder", SerialPolicyTypeEnum.KRYO5);
//         remoteCacheDefault.put("expire-after-write-in-millis", 5000);
//
//         Map<String, Object> properties = new HashMap<>();
//         properties.put("local-cache.default", localCacheDefault);
//         properties.put("remote-cache.default", remoteCacheDefault);
//
//         return properties;
//     }
//
//     @Autowired
//     public void setEnvironment(ConfigurableEnvironment environment) {
//         this.environment = environment;
//     }
// }
