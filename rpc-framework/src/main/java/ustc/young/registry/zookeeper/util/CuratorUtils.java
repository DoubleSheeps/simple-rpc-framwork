package ustc.young.registry.zookeeper.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.utils.PropertiesFileUtil;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author YoungSheep
 * @description 基于Curator的zookeeper客户端 工具类
 * @date 2024-03-28 15:07
 **/
@Slf4j
public final class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/young-rpc";
    //携带传输服务的地址Map eg:HelloService->[socket/127.0.0.1:9999]
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //通过ConcurrentHashMap.newKeySet()创建一个线程安全的Set
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private CuratorUtils(){
    }

    /**
     * 创建持久化节点
     * @param zkClient zookeeper客户端
     * @param path 路径，eg：/young-rpc/ustc.young.HelloService/socket/127.0.0.1:9999
     */
    public static void createPersistentNode(CuratorFramework zkClient,String path){
        try {
            if(REGISTERED_PATH_SET.contains(path)||zkClient.checkExists().forPath(path)!=null){
                log.info("节点已经存在，path:[{}]",path);
            }else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("创建持久化节点成功，path:[{}]",path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("创建持久化节点失败，path:[{}]",path);
        }
    }


    /**
     * 获取子节点
     * @param zkClient 客户端
     * @param rpcServiceName 节点名称（服务名）
     * @param transportName 传输服务名称
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient,String rpcServiceName,String transportName){
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName+"/"+transportName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName+"/"+transportName);
        }
        List<String> result = null;
        String servicePath = getServicePath(rpcServiceName,transportName);
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName+"/"+transportName,result);
            registerWatcher(rpcServiceName,zkClient);
        } catch (Exception e) {
            log.error("获取子节点失败，path:[{}]",servicePath);
        }
        return result;
    }

    /**
     * 清空某个服务地址相关的所有节点
     * @param zkClient 客户端
     * @param inetSocketAddress 服务地址
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().parallel().forEach(p->{
            try{
                if(p.endsWith(inetSocketAddress.toString())){
                    log.info("清空注册机中的节点，path:[{}]",p);
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("清空注册机中的节点失败，path:[{}]",p);
            }
        });
    }

    /**
     * 获取zookeeper客户端，已启动直接返回，未启动则从配置文件中查找是否存在连接地址，通过失败重试策略连接，并超时等待30秒
     * @return zookeeper客户端
     */
    public static CuratorFramework getZkClient(){
        //已启动则直接返回
        if(zkClient!=null&&zkClient.getState()== CuratorFrameworkState.STARTED){
            log.info("zk客户端已启动");
            return zkClient;
        }
        //否则通过重试策略启动
        //检测是否存在配置文件
        Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties!=null && properties.getProperty(RpcConfigEnum.ZOOKEEPER_ADDRESS.getPropertyValue())!=null
                ? properties.getProperty(RpcConfigEnum.ZOOKEEPER_ADDRESS.getPropertyValue())
                : DEFAULT_ZOOKEEPER_ADDRESS;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME,MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            //连接上zookeeper 最多等待30秒
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                throw new RuntimeException("连接zookeeper超时。");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return zkClient;
    }


    /**
     * 为节点注册一个监听器，如果发生变化就更新到SERVICE_ADDRESS_MAP中
     * @param rpcServiceName 节点服务名称
     * @param zkClient 客户端
     * @throws Exception
     */
    private static void registerWatcher(String rpcServiceName,CuratorFramework zkClient) throws Exception {
        String servicePath = getServicePath(rpcServiceName);
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient,servicePath,true);
        PathChildrenCacheListener pathChildrenCacheListener = ((curatorFramework, pathChildrenCacheEvent) -> {
            List<String> transportNodes = curatorFramework.getChildren().forPath(servicePath);
            for(String transport:transportNodes){
                List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath+"/"+transport);
                SERVICE_ADDRESS_MAP.put(rpcServiceName+"/"+transport,serviceAddresses);
            }
        });
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    private static String getServicePath(String rpcServiceName,String transportName){
        return ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + "/" + transportName;
    }

    private static String getServicePath(String rpcServiceName){
        return ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
    }

    private static List<String> getNodesByTransport(List<String> nodes,String transportName){
        if(nodes==null){
            return null;
        }
        for (String s:nodes){
            log.info("node :{},transport:{}",s,transportName);
        }
        return nodes.stream().filter(node->node.startsWith(transportName)).collect(Collectors.toList());
    }

}
