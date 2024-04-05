package ustc.young.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import ustc.young.factory.SingletonFactory;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.remoting.handler.RpcRequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:45
 **/
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;


    public SocketRpcRequestHandlerRunnable(Socket socket){
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("socket服务器通过线程[{}]处理客户端消息",Thread.currentThread().getName());
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result,rpcRequest.getRequestId()));
        }catch (IOException | ClassNotFoundException e){
            log.error("socket服务器通讯出现异常，原因是{}",e.getMessage());
        }
    }
}
