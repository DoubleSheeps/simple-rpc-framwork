package ustc.young;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ustc.young.annotations.RpcScan;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.remoting.transport.netty.server.NettyServer;
import ustc.young.serviceImpl.HelloImpl2;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 17:18
 **/
@RpcScan(basePackage = {"ustc.young"})
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = (NettyServer) applicationContext.getBean("nettyServer");
        Hello hello = new HelloImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .service(hello)
                .group("test")
                .version("2.0")
                .transport(RpcRequestTransportEnum.NETTY.getName()).build();
        nettyServer.registerService(rpcServiceConfig);
        nettyServer.run();
    }
}