package com.yat.cache.core.support.encoders;

import com.yat.cache.autoconfigure.properties.enums.SerialPolicyTypeEnum;
import com.yat.cache.core.CacheValueHolder;
import com.yat.cache.core.exception.CacheEncodeException;
import com.yat.cache.core.support.CacheMessage;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

/**
 * ClassName AbstractJsonEncoder
 * <p>Description 抽象 JSON 编码器:提供了基于 JSON 的值编码功能</p>
 *
 * @author Yat
 * Date 2024/8/22 13:14
 * version 1.0
 */
public abstract class AbstractJsonEncoder extends AbstractValueEncoder {

    public AbstractJsonEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        try {
            JsonData[] data = encode(value);
            int len = len(data);
            byte[] buffer = useIdentityNumber ? new byte[len + 4] : new byte[len];
            int index = 0;
            if (useIdentityNumber) {
                index = writeInt(buffer, index, SerialPolicyTypeEnum.GSON.getCode());
            }
            if (data == null) {
                writeShort(buffer, index, -1);
            } else {
                index = writeShort(buffer, index, data.length);
                for (JsonData d : data) {
                    if (d == null) {
                        index = writeShort(buffer, index, -1);
                    } else {
                        index = writeShort(buffer, index, d.getClassName().length);
                        index = writeBytes(buffer, index, d.getClassName());
                        index = writeInt(buffer, index, d.getData().length);
                        index = writeBytes(buffer, index, d.getData());
                    }
                }
            }
            return buffer;
        } catch (Throwable e) {
            String message = "Json Encode error. msg=" + e.getMessage();
            throw new CacheEncodeException(message, e);
        }
    }

    private JsonData[] encode(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CacheValueHolder h) {
            Object bizObject = h.getValue();
            h.setValue(null);
            JsonData[] result = new JsonData[2];
            result[0] = encodeJsonData(h);
            result[1] = encodeJsonData(bizObject);
            h.setValue(bizObject);
            return result;
        } else if (value instanceof CacheMessage cm) {
            Object[] keys = cm.getKeys();
            cm.setKeys(null);
            JsonData[] result = keys == null ? new JsonData[1] : new JsonData[keys.length + 1];
            result[0] = encodeJsonData(cm);
            if (keys != null) {
                for (int i = 0; i < keys.length; i++) {
                    result[i + 1] = encodeJsonData(keys[i]);
                }
            }
            cm.setKeys(keys);
            return result;
        } else {
            return new JsonData[]{encodeJsonData(value)};
        }
    }

    private int len(JsonData[] data) {
        if (data == null) {
            return 2;
        }
        int x = 2;
        for (JsonData d : data) {
            if (d == null) {
                x += 2;
            } else {
                x += 2 + d.getClassName().length + 4 + d.getData().length;
            }
        }
        return x;
    }

    private int writeInt(byte[] buf, int index, int value) {
        buf[index] = (byte) (value >> 24 & 0xFF);
        buf[index + 1] = (byte) (value >> 16 & 0xFF);
        buf[index + 2] = (byte) (value >> 8 & 0xFF);
        buf[index + 3] = (byte) (value & 0xFF);
        return index + 4;
    }

    private int writeShort(byte[] buf, int index, int value) {
        buf[index] = (byte) (value >> 8 & 0xFF);
        buf[index + 1] = (byte) (value & 0xFF);
        return index + 2;
    }

    private int writeBytes(byte[] buf, int index, byte[] data) {
        System.arraycopy(data, 0, buf, index, data.length);
        return index + data.length;
    }

    private JsonData encodeJsonData(Object value) {
        if (value == null) {
            return null;
        }
        JsonData jsonData = new JsonData();
        jsonData.setClassName(value.getClass().getName().getBytes(StandardCharsets.UTF_8));
        jsonData.setData(encodeSingleValue(value));
        return jsonData;
    }

    protected abstract byte[] encodeSingleValue(Object value);

    @Setter
    @Getter
    private static class JsonData {
        private byte[] className;
        private byte[] data;

    }
}
