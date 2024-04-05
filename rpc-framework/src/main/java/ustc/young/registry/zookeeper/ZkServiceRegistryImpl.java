package ustc.young.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import ustc.young.registry.ServiceRegistry;
import ustc.young.registry.zookeeper.util.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * @author YoungSheep
 * @description 基于zookeeper实现的注册服务
 * @date 2024-03-28 15:04
 **/
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress,String transportService) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName+"/" +transportService + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,servicePath);
    }
}
