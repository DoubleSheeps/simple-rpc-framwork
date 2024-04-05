package ustc.young.registry;

import ustc.young.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 11:20
 **/
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务
     * @param rpcServiceName 服务名
     * @param inetSocketAddress 地址
     * @param transportService 传输服务工具 socke/netty
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress,String transportService);
}
