package ustc.young.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import ustc.young.enums.DefaultConfigEnum;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.enums.SerializeEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.factory.SingletonFactory;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.remoting.handler.RpcRequestHandler;
import ustc.young.serialize.Serializer;
import ustc.young.utils.PropertiesFileUtil;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:45
 **/
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;
    private static final int BODY_MARK_LENGTH = 4;
    private static final int HEAD_LENGTH = 1;


    public SocketRpcRequestHandlerRunnable(Socket socket){
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("socket服务器通过线程[{}]处理客户端消息",Thread.currentThread().getName());
//        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
//            Object result = rpcRequestHandler.handle(rpcRequest);
//            objectOutputStream.writeObject(RpcResponse.success(result,rpcRequest.getRequestId()));
        try(DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            int length = dataInputStream.readInt();
            byte serializeCode = dataInputStream.readByte();
            String serializeName = SerializeEnum.getName(serializeCode);
            if(serializeName==null){
                log.error("传输数据的序列化协议字段不合法");
            }
            byte[] body = new byte[length-HEAD_LENGTH];
            dataInputStream.read(body,0,length-HEAD_LENGTH);
            Serializer serializer1 = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializeName);
            Class<?> clazz = RpcRequest.class;
            Object rpcRequest = serializer1.deserialize(body, clazz);
            Object result = rpcRequestHandler.handle((RpcRequest) rpcRequest);
            Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
            String serializerName =  properties!=null && properties.getProperty(RpcConfigEnum.SERIALIZER.getPropertyValue())!=null
                    ? properties.getProperty(RpcConfigEnum.SERIALIZER.getPropertyValue())
                    : DefaultConfigEnum.DEFAULT_SERIALIZER.getName();
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerName);
            byte[] responseBody = serializer.serialize(RpcResponse.success(result,((RpcRequest)rpcRequest).getRequestId()));
            int dataLength = responseBody.length+1;
            dataOutputStream.writeInt(dataLength);
            dataOutputStream.writeByte(SerializeEnum.KRYO.getCode());
            dataOutputStream.write(responseBody);
        }catch (IOException e){
            log.error("socket服务器通讯出现异常，原因是{}",e.getMessage());
        }
    }
}
