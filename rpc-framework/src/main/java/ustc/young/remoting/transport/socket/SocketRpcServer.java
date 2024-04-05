package ustc.young.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import ustc.young.annotations.RpcService;
import ustc.young.config.CustomShutdownHook;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.factory.SingletonFactory;
import ustc.young.provider.ServiceProvider;
import ustc.young.provider.impl.ZkServiceProviderImpl;
import ustc.young.remoting.transport.RpcServer;
import ustc.young.utils.PropertiesFileUtil;
import ustc.young.utils.threadpool.ThreadPoolFactoryUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 13:51
 **/
@Slf4j
public class SocketRpcServer implements RpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;
    private final int port;
    private static final int DEFAULT_PORT=4321;

    public SocketRpcServer(){
        Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        this.port =  properties!=null && properties.getProperty(RpcConfigEnum.SERVER_PORT.getPropertyValue())!=null
                ? Integer.parseInt(properties.getProperty(RpcConfigEnum.SERVER_PORT.getPropertyValue()))
                : DEFAULT_PORT;
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    @Override
    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig,port);
    }

    @Override
    public void run()  {
        CustomShutdownHook.getCustomShutdownHook().clearAll(port);
        try(ServerSocket serverSocket = new ServerSocket()){
            String host = InetAddress.getLocalHost().getHostAddress();
            serverSocket.bind(new InetSocketAddress(host,port));
            Socket socket;
            while ((socket=serverSocket.accept())!=null){
                log.info("客户端[{}]已连接",socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        }catch (IOException e){
            log.error("ServerSocket启动出现异常，原因是{}",e.getMessage());
        }
    }

}
