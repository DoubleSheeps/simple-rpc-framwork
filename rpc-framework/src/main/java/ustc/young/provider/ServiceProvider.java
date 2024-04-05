package ustc.young.provider;

import ustc.young.config.RpcServiceConfig;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:50
 **/
public interface ServiceProvider {
    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig,int port);
}
