package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 13:47
 **/
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseStatusEnum {
    SUCCESS(200, "The remote call is successful"),
    PING(201, "ping"),
    PONG(202, "pong"),
    FAIL(500, "The remote call is fail");
    private final int code;

    private final String message;
}
