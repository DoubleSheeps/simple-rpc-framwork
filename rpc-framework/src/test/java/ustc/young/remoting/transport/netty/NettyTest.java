package ustc.young.remoting.transport.netty;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.proxy.RpcClientProxy;
import ustc.young.remoting.transport.Hello;
import ustc.young.remoting.transport.HelloImpl;
import ustc.young.remoting.transport.RpcRequestTransport;
import ustc.young.remoting.transport.netty.client.NettyClient;
import ustc.young.remoting.transport.netty.server.NettyServer;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 15:58
 **/
@Slf4j
public class NettyTest {

    @Test
    public void NettyServerTest(){
        Hello hello = new HelloImpl();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(hello);
        rpcServiceConfig.setGroup("test");
        rpcServiceConfig.setVersion("1.0");
        rpcServiceConfig.setTransport(RpcRequestTransportEnum.NETTY.getName());
        NettyServer server = new NettyServer();
        server.registerService(rpcServiceConfig);
        server.run();
    }

    @Test
    public void NettyClientTest(){
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setGroup("test");
        rpcServiceConfig.setVersion("1.0");
        rpcServiceConfig.setTransport(RpcRequestTransportEnum.NETTY.getName());
        RpcRequestTransport requestTransport = new NettyClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(requestTransport,rpcServiceConfig);
        Hello hello = rpcClientProxy.getProxy(Hello.class);
        String result = hello.hello("young");
        log.info("客户端连接并调用方法成功，响应：{}",result);
    }
}
