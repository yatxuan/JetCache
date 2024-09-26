package com.yat.cache.core.support.encoders;

import lombok.Getter;

import java.util.function.Function;

/**
 * ClassName AbstractValueEncoder
 * <p>Description 抽象值编码器</p>
 * <p>
 * 此抽象类提供了基本的值编码功能，并实现了 {@link Function} 和 {@link ValueEncoders} 接口。
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 13:10
 * version 1.0
 */
@Getter
public abstract class AbstractValueEncoder implements Function<Object, byte[]>, ValueEncoders {

    /**
     * 标识是否使用标识号进行序列化。
     */
    protected boolean useIdentityNumber;

    public AbstractValueEncoder(boolean useIdentityNumber) {
        this.useIdentityNumber = useIdentityNumber;
    }

}
