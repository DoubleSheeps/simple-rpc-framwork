package ustc.young.loadbalance.loadbalancer;

import ustc.young.loadbalance.AbstractLoadBalance;
import ustc.young.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 17:37
 **/
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceUrls, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrls.get(random.nextInt(serviceUrls.size()));
    }
}
