package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-05 15:26
 **/
@AllArgsConstructor
@Getter
public enum DefaultConfigEnum {
    DEFAULT_LOAD_BALANCE("random"),
    DEFAULT_SERIALIZER("kryo"),
    DEFAULT_SERVICE_DISCOVERY("zookeeper"),
    DEFAULT_SERVICE_REGISTRY("zookeeper"),
    DEFAULT_TRANSPORT("netty");
    private String name;
}
