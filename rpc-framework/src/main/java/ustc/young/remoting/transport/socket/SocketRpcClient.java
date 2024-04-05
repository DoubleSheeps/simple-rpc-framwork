package ustc.young.remoting.transport.socket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.enums.ServiceDiscoveryEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.registry.ServiceDiscovery;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.transport.RpcRequestTransport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:36
 **/
@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest, RpcRequestTransportEnum.SOCKET.getName());
        try(Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        }catch (IOException | ClassNotFoundException e){
            log.error("调用服务失败，原因是：{}",e.getMessage());
        }
        return null;
    }
}
