package ustc.young.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import ustc.young.config.CustomShutdownHook;
import ustc.young.config.RpcServiceConfig;
import ustc.young.factory.SingletonFactory;
import ustc.young.provider.ServiceProvider;
import ustc.young.provider.impl.ZkServiceProviderImpl;
import ustc.young.utils.threadpool.ThreadPoolFactoryUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 13:51
 **/
@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;
    private final int port;

    public SocketRpcServer(int port){
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.port = port;
    }

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig,port);
    }

    public void start()  {
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
