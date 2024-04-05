package ustc.young.remoting.transport;

import ustc.young.config.RpcServiceConfig;
import ustc.young.extension.SPI;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-05 16:07
 **/
@SPI
public interface RpcServer {
    void registerService(RpcServiceConfig rpcServiceConfig);
    void run();
}
