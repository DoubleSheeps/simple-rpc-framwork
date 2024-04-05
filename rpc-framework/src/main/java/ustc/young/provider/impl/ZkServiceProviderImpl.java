package ustc.young.provider.impl;

import lombok.extern.slf4j.Slf4j;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.ServiceRegistryEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.provider.ServiceProvider;
import ustc.young.registry.ServiceRegistry;
import ustc.young.utils.StringUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:56
 **/
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    private final Map<String,Object> serviceMap;
    private final Set<String> registeredService;

    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl(){
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("成功添加服务：{}，接口为：{}",rpcServiceName,rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if(service==null){
            throw new RuntimeException("未找到可用的服务！");
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig,int port) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            if (StringUtil.isBlank(rpcServiceConfig.getTransport())){
                log.error("请提供传输服务提供者！");
            }
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(),new InetSocketAddress(host,port), rpcServiceConfig.getTransport());
        } catch (UnknownHostException e) {
            log.error("发布服务时无法获取到本机IP地址");
        }
    }
}
