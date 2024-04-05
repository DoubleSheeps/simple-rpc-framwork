package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YoungSheep
 * @description RPC配置相关枚举类
 * @date 2024-03-28 15:51
 **/
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    RPC_CONFIG_PATH("rpc.properties"),
    ZOOKEEPER_ADDRESS("rpc.zookeeper.address"),
    NETTY_PORT("rpc.netty.port");
    private final String propertyValue;
}
