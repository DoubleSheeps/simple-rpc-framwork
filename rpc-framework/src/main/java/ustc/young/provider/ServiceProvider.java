package ustc.young.provider;

import ustc.young.config.RpcServiceConfig;

/**
 * @author YoungSheep
 * @description 服务端服务注册的代理，不仅可以代理向注册中心注册，还缓存了完整的服务提供类供调用时调用
 * @date 2024-03-29 12:50
 **/
public interface ServiceProvider {
    /**
     * 向缓存中添加服务信息
     * @param rpcServiceConfig 完整的服务信息
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 从缓存中查找提供服务的类对象
     * @param rpcServiceName 服务名
     * @return 提供服务的类对象
     */
    Object getService(String rpcServiceName);

    /**
     * 代理向注册中心发布服务信息，同时还要缓存服务名和提供服务类的映射关系
     * @param rpcServiceConfig 完整的服务信息
     * @param port 服务器的端口
     */
    void publishService(RpcServiceConfig rpcServiceConfig,int port);
}
