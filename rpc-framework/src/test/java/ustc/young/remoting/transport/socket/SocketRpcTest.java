package ustc.young.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.proxy.RpcClientProxy;
import ustc.young.remoting.transport.Hello;
import ustc.young.remoting.transport.HelloImpl;
import ustc.young.remoting.transport.RpcRequestTransport;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 14:40
 **/
@Slf4j
class SocketRpcTest {

    @Test
    public void socketRpcServer(){
        Hello hello = new HelloImpl();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(hello);
        rpcServiceConfig.setGroup("test");
        rpcServiceConfig.setVersion("1.0");
        rpcServiceConfig.setTransport(RpcRequestTransportEnum.SOCKET.getName());
        SocketRpcServer socketRpcServer = new SocketRpcServer(4821);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();


    }

    @Test
    public void socketClient(){
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setGroup("test");
        rpcServiceConfig.setVersion("1.0");
        rpcServiceConfig.setTransport(RpcRequestTransportEnum.SOCKET.getName());
        RpcRequestTransport requestTransport = new SocketRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(requestTransport,rpcServiceConfig);
        Hello hello = rpcClientProxy.getProxy(Hello.class);
        String result = hello.hello("young");
        log.info("客户端连接并调用方法成功，响应：{}",result);
    }

}