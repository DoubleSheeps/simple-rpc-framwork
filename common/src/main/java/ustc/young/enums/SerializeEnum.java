package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 14:09
 **/
@Getter
@AllArgsConstructor
public enum SerializeEnum {
    KRYO((byte) 0x01,"kryo");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializeEnum c : SerializeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
