package ustc.young.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ustc.young.enums.RpcResponseStatusEnum;

import java.io.Serializable;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 13:42
 **/
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = -4568655341051180996L;
    private String requestId;
    private Integer code;
    private String message;
    private T data;
    public static <T> RpcResponse<T> success(T data,String requestId){
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseStatusEnum.SUCCESS.getCode());
        rpcResponse.setMessage(RpcResponseStatusEnum.SUCCESS.getMessage());
        rpcResponse.setRequestId(requestId);
        if(null!=data){
            rpcResponse.setData(data);
        }
        return rpcResponse;
    }

    public static <T> RpcResponse<T> fail(RpcResponseStatusEnum rpcResponseStatusEnum){
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(rpcResponseStatusEnum.getCode());
        rpcResponse.setMessage(rpcResponseStatusEnum.getMessage());
        return rpcResponse;
    }

//    @Override
//    public String toString() {
//        return "RpcResponse{" +
//                "requestId='" + requestId + '\'' +
//                ", code=" + code +
//                ", message='" + message + '\'' +
//                ", data=" + data!=null?data.toString():"{}" +
//                '}';
//    }
}
