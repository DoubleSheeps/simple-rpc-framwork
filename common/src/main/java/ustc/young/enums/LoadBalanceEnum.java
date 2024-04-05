package ustc.young.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 17:41
 **/
@Getter
@AllArgsConstructor
public enum LoadBalanceEnum {
    LOADBALANCE("loadBalance");

    private final String name;
}
