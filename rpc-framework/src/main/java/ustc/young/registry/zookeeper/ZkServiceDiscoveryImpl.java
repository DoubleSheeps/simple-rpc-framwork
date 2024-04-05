package ustc.young.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import ustc.young.enums.LoadBalanceEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.loadbalance.LoadBalance;
import ustc.young.registry.ServiceDiscovery;
import ustc.young.registry.zookeeper.util.CuratorUtils;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.utils.CollectionUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 16:22
 **/
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl(){
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.CONSISTENT_HASH.getName());
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
