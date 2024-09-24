package com.yat.cache.autoconfigure;

import com.yat.cache.autoconfigure.constants.BeanNameConstant;
import com.yat.cache.autoconfigure.init.AbstractCacheAutoInit;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

/**
 * Bean依赖管理器
 * <p>
 * 该类实现BeanFactoryPostProcessor接口，用于在Bean工厂中处理特定Bean的依赖关系
 * 具体来说，它是为了确保所有自动初始化的缓存相关Bean被正确地依赖于全局缓存配置Bean
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 22:09
 * version 1.0
 */
public class BeanDependencyManager implements BeanFactoryPostProcessor {

    /**
     * 在Bean工厂后处理阶段添加依赖关系
     * <p>
     * 此方法在Bean工厂中所有Bean的定义都已加载但尚未实例化之前被调用
     * 它的作用是为全局缓存配置Bean添加依赖，确保所有自动初始化的缓存Bean
     * 在启动时正确初始化
     * </p>
     *
     * @param beanFactory Bean工厂实例，用于获取和修改Bean定义
     * @throws BeansException 如果操作过程中发生异常
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 获取所有自动初始化缓存Bean的名称
        String[] autoInitBeanNames = beanFactory.getBeanNamesForType(AbstractCacheAutoInit.class, false, false);
        if (!ObjectUtils.isEmpty(autoInitBeanNames)) {
            // 获取全局缓存配置Bean的定义
            BeanDefinition bd = beanFactory.getBeanDefinition(BeanNameConstant.GLOBAL_CACHE_CONFIG_NAME);
            // 获取全局缓存配置Bean当前依赖的Bean名称数组
            String[] dependsOn = bd.getDependsOn();
            // 如果依赖数组为空，初始化为空数组
            if (dependsOn == null) {
                dependsOn = new String[0];
            }
            // 计算原有依赖数量
            int oldLen = dependsOn.length;
            // 扩展依赖数组，以容纳新增的自动初始化缓存Bean
            dependsOn = Arrays.copyOf(dependsOn, dependsOn.length + autoInitBeanNames.length);
            // 将自动初始化缓存Bean名称复制到依赖数组中
            System.arraycopy(autoInitBeanNames, 0, dependsOn, oldLen, autoInitBeanNames.length);
            // 更新Bean定义的依赖关系
            bd.setDependsOn(dependsOn);
        }
    }

}
