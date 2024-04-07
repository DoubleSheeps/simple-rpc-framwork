package ustc.young;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ustc.young.annotations.RpcScan;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.DefaultConfigEnum;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.remoting.transport.RpcServer;
import ustc.young.serviceImpl.HelloImpl2;
import ustc.young.utils.PropertiesFileUtil;

import java.util.Properties;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 17:18
 **/
@RpcScan(basePackage = {"ustc.young"})
public class ServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ServerMain.class);
//        NettyServer nettyServer = (NettyServer) applicationContext.getBean("nettyServer");
//        Hello hello = new HelloImpl2();
//        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
//                .service(hello)
//                .group("test")
//                .version("2.0")
//                .transport(RpcRequestTransportEnum.NETTY.getName()).build();
//        nettyServer.registerService(rpcServiceConfig);
//        nettyServer.run();
        Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String transportName = properties!=null && properties.getProperty(RpcConfigEnum.TRANSPORT.getPropertyValue())!=null
                ? properties.getProperty(RpcConfigEnum.TRANSPORT.getPropertyValue())
                : DefaultConfigEnum.DEFAULT_TRANSPORT.getName();
        RpcServer rpcServer = ExtensionLoader.getExtensionLoader(RpcServer.class).getExtension(transportName);
        Hello hello = new HelloImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .service(hello)
                .group("test")
                .version("2.0")
                .transport(transportName).build();
        rpcServer.registerService(rpcServiceConfig);
        rpcServer.run();
    }
}