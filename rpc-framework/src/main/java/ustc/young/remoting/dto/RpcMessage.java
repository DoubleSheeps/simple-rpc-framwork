package ustc.young.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-03 15:36
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcMessage<T> {
    private int type;
    private T data;
}
