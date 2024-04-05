package ustc.young.remoting.transport;

import ustc.young.extension.SPI;
import ustc.young.remoting.dto.RpcRequest;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:33
 **/
@SPI
public interface RpcRequestTransport {
    /**
     * 发送Rpc请求并获取响应接口
     * @param rpcRequest 请求消息体
     * @return 响应结果
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
