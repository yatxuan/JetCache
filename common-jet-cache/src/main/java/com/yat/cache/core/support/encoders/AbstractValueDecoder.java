package com.yat.cache.core.support.encoders;

import com.yat.cache.core.exception.CacheEncodeException;
import com.yat.cache.core.support.DecoderMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.function.Function;

/**
 * ClassName AbstractValueDecoder
 * <p>Description 抽象值解码器类，实现了基于字节数组的解码逻辑。</p>
 *
 * @author Yat
 * Date 2024/8/22 17:42
 * version 1.0
 */
public abstract class AbstractValueDecoder implements Function<byte[], Object>, ValueEncoders {

    /**
     * 解码器映射表
     */
    @Setter
    private DecoderMap decoderMap = DecoderMap.defaultInstance();
    /**
     * 标志是否使用标识号进行解码
     */
    @Getter
    protected boolean useIdentityNumber;

    /**
     * 初始化是否使用标识号。
     *
     * @param useIdentityNumber 是否使用唯一序列号标识号
     */
    public AbstractValueDecoder(Boolean useIdentityNumber) {
        this.useIdentityNumber = useIdentityNumber;
    }

    /**
     * 实现 Function 接口的 apply 方法，执行解码操作。
     *
     * @param buffer 待解码的字节数组
     * @return 解码后的对象
     */
    @Override
    public Object apply(byte[] buffer) {
        try {
            if (useIdentityNumber) {
                decoderMap.initDefaultDecoder();
                int identityNumber = parseHeader(buffer);
                AbstractValueDecoder decoder = decoderMap.getDecoder(identityNumber);
                Objects.requireNonNull(decoder, "no decoder for identity number:" + identityNumber);
                return decoder.doApply(buffer);
            } else {
                return doApply(buffer);
            }
        } catch (Throwable e) {
            throw new CacheEncodeException("decode error", e);
        }
    }

    /**
     * 解析字节数组头部信息。
     *
     * @param buf 字节数组
     * @return 头部整数值
     */
    protected int parseHeader(byte[] buf) {
        int x = 0;
        x = x | (buf[0] & 0xFF);
        x <<= 8;
        x = x | (buf[1] & 0xFF);
        x <<= 8;
        x = x | (buf[2] & 0xFF);
        x <<= 8;
        x = x | (buf[3] & 0xFF);
        return x;
    }

    /**
     * 执行实际的解码逻辑。
     *
     * @param buffer 待解码的字节数组
     * @return 解码后的对象
     * @throws Exception 如果解码过程中发生错误
     */
    protected abstract Object doApply(byte[] buffer) throws Exception;

}
