package com.yat.cache.anno.support;

import lombok.Getter;
import lombok.Setter;

/**
 * CacheInvalidateAnnoConfig 类是缓存失效注解配置的实体类，它继承自 CacheAnnoConfig，
 * 提供了关于缓存失效操作的额外配置选项。该类使用了 Lombok 注解来简化 getter 和 setter 方法的生成。
 *
 * @author Yat
 * Date 2024/8/22 22:02
 * version 1.0
 */
@Setter
@Getter
public class CacheInvalidateAnnoConfig extends CacheAnnoConfig {

    /**
     * 指示是否支持多键缓存失效。如果设置为 true，则缓存失效操作可以一次接受多个键，
     * 提高缓存管理的效率。默认情况下为 false，即不支持多键操作。
     */
    private boolean multi;

}
