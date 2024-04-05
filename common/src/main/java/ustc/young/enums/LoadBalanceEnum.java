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
    RANDOM("random"),
    CONSISTENT_HASH("consistent-hash");

    private final String name;
}
