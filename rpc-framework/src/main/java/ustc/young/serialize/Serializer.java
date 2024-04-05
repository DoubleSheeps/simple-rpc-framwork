package ustc.young.serialize;

import ustc.young.extension.SPI;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 13:43
 **/
@SPI
public interface Serializer {
    /**
     * 序列化
     * @param obj 对象
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param clazz 类
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
