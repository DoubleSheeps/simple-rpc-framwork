package ustc.young.proxy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ustc.young.config.RpcServiceConfig;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.remoting.transport.RpcRequestTransport;
import ustc.young.remoting.transport.netty.client.NettyClient;
import ustc.young.remoting.transport.socket.SocketRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 15:35
 **/
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcRequestTransport requestTransport;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport requestTransport,RpcServiceConfig rpcServiceConfig){
        this.requestTransport = requestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }



    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]",method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if(requestTransport instanceof SocketRpcClient){
            rpcResponse = (RpcResponse<Object>) requestTransport.sendRpcRequest(rpcRequest);
        }else if(requestTransport instanceof NettyClient){
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) requestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
//        rpcResponse = (RpcResponse<Object>) requestTransport.sendRpcRequest(rpcRequest);
        return rpcResponse.getData();
    }

}
