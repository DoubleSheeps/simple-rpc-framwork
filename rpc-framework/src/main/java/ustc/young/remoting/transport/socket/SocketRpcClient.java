package ustc.young.remoting.transport.socket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ustc.young.enums.DefaultConfigEnum;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.enums.SerializeEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.registry.ServiceDiscovery;
import ustc.young.remoting.dto.RpcMessage;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.remoting.transport.RpcRequestTransport;
import ustc.young.serialize.Serializer;
import ustc.young.utils.PropertiesFileUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:36
 **/
@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    private static final int BODY_MARK_LENGTH = 4;
    private static final int HEAD_LENGTH = 1;
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String serviceDiscoveryName = properties!=null && properties.getProperty(RpcConfigEnum.SERVICE_DISCOVERY.getPropertyValue())!=null
                ? properties.getProperty(RpcConfigEnum.SERVICE_DISCOVERY.getPropertyValue())
                : DefaultConfigEnum.DEFAULT_SERVICE_DISCOVERY.getName();
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(serviceDiscoveryName);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest, RpcRequestTransportEnum.SOCKET.getName());
        try(Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
            String serializerName =  properties!=null && properties.getProperty(RpcConfigEnum.SERIALIZER.getPropertyValue())!=null
                    ? properties.getProperty(RpcConfigEnum.SERIALIZER.getPropertyValue())
                    : DefaultConfigEnum.DEFAULT_SERIALIZER.getName();
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerName);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            byte[] body = serializer.serialize(rpcRequest);
            int dataLength = body.length+1;
            dataOutputStream.writeInt(dataLength);
            dataOutputStream.writeByte(SerializeEnum.KRYO.getCode());
            dataOutputStream.write(body);
//            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int length = dataInputStream.readInt();
            byte serializeCode = dataInputStream.readByte();
            String serializeName = SerializeEnum.getName(serializeCode);
            if(serializeName==null){
                log.error("传输数据的序列化协议字段不合法");
            }
            byte[] responseBody = new byte[length-HEAD_LENGTH];
            dataInputStream.read(responseBody,0,length-HEAD_LENGTH);
            Serializer serializer1 = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializeName);
            Class<?> clazz = RpcResponse.class;
            Object obj = serializer1.deserialize(responseBody, clazz);
            return obj;
        }catch (IOException e){
            log.error("调用服务失败，原因是：{}",e.getMessage());
        }
        return null;
    }
}
