package ustc.young.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 14:51
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 4011360030596969627L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;


    public String getRpcServiceName(){
        return this.getInterfaceName()+this.getGroup()+this.getVersion();
    }
}
