package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:39
 **/
@AllArgsConstructor
@Getter
public enum ServiceRegistryEnum {
    ZK("zookeeper");

    private final String name;
}
