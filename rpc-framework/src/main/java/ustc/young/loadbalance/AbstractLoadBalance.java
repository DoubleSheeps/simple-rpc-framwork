package ustc.young.loadbalance;

import ustc.young.remoting.dto.RpcRequest;
import ustc.young.utils.CollectionUtil;

import java.util.List;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 17:34
 **/
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceUrls, RpcRequest rpcRequest) {
        if(CollectionUtil.isEmpty(serviceUrls)){
            return null;
        }
        if(serviceUrls.size()==1){
            return serviceUrls.get(0);
        }
        return doSelect(serviceUrls,rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceUrls,RpcRequest rpcRequest);
}
