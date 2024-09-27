package com.yat.cache.anno.support;

import com.yat.cache.anno.method.SpringCacheContext;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.support.StatInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.function.Consumer;

/**
 * Spring配置提供者，用于在Spring环境中初始化和提供缓存配置上下文
 * 该类继承自ConfigProvider，并实现了ApplicationContextAware接口，以便在Spring环境中
 * 注入Application上下文
 *
 * @author Yat
 * Date 2024/8/22 22:07
 * version 1.0
 */
public class SpringConfigProvider extends ConfigProvider implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 初始化时设置默认的编码解析器和键转换器解析器
     */
    public SpringConfigProvider() {
        super();
        encoderParser = new DefaultSpringEncoderParser();
        keyConvertorParser = new DefaultSpringKeyConvertorParser();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化配置提供者
     * 如果编码解析器和键转换器解析器实现了ApplicationContextAware，就注入Application上下文
     */
    @Override
    protected void doInit() {
        if (encoderParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) encoderParser).setApplicationContext(applicationContext);
        }
        if (keyConvertorParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) keyConvertorParser).setApplicationContext(applicationContext);
        }
        super.doInit();
    }

    /**
     * 创建新的缓存上下文
     *
     * @param jetCacheManager 缓存管理器
     * @return 返回Spring环境下的缓存上下文实例
     */
    @Override
    public CacheContext newContext(JetCacheManager jetCacheManager) {
        return new SpringCacheContext(jetCacheManager, this, globalCacheConfig, applicationContext);
    }

    /**
     * 设置编码解析器
     *
     * @param encoderParser 编码解析器
     */
    @Autowired(required = false)
    @Override
    public void setEncoderParser(EncoderParser encoderParser) {
        super.setEncoderParser(encoderParser);
    }

    /**
     * 设置键转换器解析器
     *
     * @param keyConvertorParser 键转换器解析器
     */
    @Autowired(required = false)
    @Override
    public void setKeyConvertorParser(KeyConvertorParser keyConvertorParser) {
        super.setKeyConvertorParser(keyConvertorParser);
    }

    /**
     * 设置指标回调函数
     *
     * @param metricsCallback 指标信息的消费者
     */
    @Autowired(required = false)
    @Override
    public void setMetricsCallback(Consumer<StatInfo> metricsCallback) {
        super.setMetricsCallback(metricsCallback);
    }

}
