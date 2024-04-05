package ustc.young.remoting.transport.netty.client;

import ustc.young.remoting.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-03 14:48
 **/
public class ResponseFuture {
    private final ConcurrentHashMap<String, CompletableFuture<RpcResponse<Object>>> completableResponseFuture = new ConcurrentHashMap<>();

    public void put(String requestId,CompletableFuture<RpcResponse<Object>> responseCompletableFuture){
        completableResponseFuture.put(requestId,responseCompletableFuture);
    }

    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> rpcResponseCompletableFuture = completableResponseFuture.remove(rpcResponse.getRequestId());
        if(rpcResponseCompletableFuture!=null&&!rpcResponseCompletableFuture.isDone()){
            rpcResponseCompletableFuture.complete(rpcResponse);
        }
    }
}
