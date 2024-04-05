package ustc.young.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import ustc.young.enums.DefaultConfigEnum;
import ustc.young.enums.LoadBalanceEnum;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.loadbalance.LoadBalance;
import ustc.young.registry.ServiceDiscovery;
import ustc.young.registry.zookeeper.util.CuratorUtils;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.utils.CollectionUtil;
import ustc.young.utils.PropertiesFileUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 16:22
 **/
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl(){
        Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String loadBalanceName =  properties!=null && properties.getProperty(RpcConfigEnum.LOAD_BALANCE.getPropertyValue())!=null
                ? properties.getProperty(RpcConfigEnum.LOAD_BALANCE.getPropertyValue())
                : DefaultConfigEnum.DEFAULT_LOAD_BALANCE.getName();
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalanceName);
    }
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest, String transport) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrls = CuratorUtils.getChildrenNodes(zkClient,rpcServiceName, transport);
        if(CollectionUtil.isEmpty(serviceUrls)){
            throw new RuntimeException("没有找到可用的服务["+rpcServiceName+"]。");
        }
        for (String s:serviceUrls){
            log.info("serviceUrls:{}",s);
        }
        //负载均衡
        String targetUrl = loadBalance.selectServiceAddress(serviceUrls,rpcRequest);
        String[] socketAddressArray = targetUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host,port);
    }
}
