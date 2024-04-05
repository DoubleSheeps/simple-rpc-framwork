package ustc.young.registry;

import ustc.young.extension.SPI;
import ustc.young.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 14:49
 **/
@SPI
public interface ServiceDiscovery {
    /**
     * 通过服务名称获取服务
     * @param rpcRequest 服务请求数据对象
     * @param transport 传输服务提供者
     * @return 服务地址
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest, String transport);
}
