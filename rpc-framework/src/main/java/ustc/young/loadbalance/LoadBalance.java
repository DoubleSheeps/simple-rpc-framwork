package ustc.young.loadbalance;

import ustc.young.extension.SPI;
import ustc.young.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @author YoungSheep
 * @description 负载均衡策略
 * @date 2024-03-28 17:30
 **/
@SPI
public interface LoadBalance {
    /**
     * 选择一个负载均衡的服务地址
     * @param serviceUrls 服务地址集合
     * @return 选中的服务地址
     */
    String selectServiceAddress(List<String> serviceUrls, RpcRequest rpcRequest);
}
