package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-03 15:43
 **/
@Getter
@AllArgsConstructor
public enum RpcMessageTypeEnum {
    PING(1),
    PONG(2),
    REQUEST(3),
    RESPONSE(4);
    private final int type;
}
