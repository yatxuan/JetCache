package com.yat.cache.anno.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

import java.util.ArrayList;
import java.util.List;

/**
 * ConfigSelector类用于根据AdviceMode选择合适的配置导入
 * 它扩展了AdviceModeImportSelector，并针对EnableMethodCache注解进行了配置
 * 该类的主要作用是决定使用代理模式还是切面模式，并返回相应的配置类
 *
 * @author Yat
 * Date 2024/8/22 21:13
 * version 1.0
 */
@SuppressWarnings("all")
public class ConfigSelector extends AdviceModeImportSelector<EnableJetMethodCache> {

    /**
     * 根据AdviceMode选择并返回需要导入的配置类
     *
     * @param adviceMode 缓存顾问模式，决定了使用哪种缓存实现方式
     * @return String[] 包含所需配置类的字符串数组
     */
    @Override
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return getProxyImports();
            case ASPECTJ:
//                return getAspectJImports();
            default:
                return null;
        }
    }

    /**
     * 当AdviceMode设置为PROXY时返回所需的配置导入
     * <p>
     * 注意：如果可用，添加必要的JSR-107导入
     * </p>
     *
     * @return String[] 包含代理模式所需配置类的字符串数组
     */
    private String[] getProxyImports() {
        List<String> result = new ArrayList<>();
        result.add(AutoProxyRegistrar.class.getName());
        result.add(JetCacheProxyConfiguration.class.getName());
        return result.toArray(new String[0]);
    }

    // /**
    //  * Return the imports to use if the {@link AdviceMode} is set to {@link AdviceMode#ASPECTJ}.
    //  * <p>Take care of adding the necessary JSR-107 import if it is available.
    //  */
    // private String[] getAspectJImports() {
    //     List<String> result = new ArrayList<String>();
    //     result.add(CACHE_ASPECT_CONFIGURATION_CLASS_NAME);
    //     return result.toArray(new String[result.size()]);
    // }
}
