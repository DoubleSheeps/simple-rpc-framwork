package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 17:01
 **/
@AllArgsConstructor
@Getter
public enum RpcRequestTransportEnum {
    NETTY("netty"),
    SOCKET("socket");

    private final String name;
}
