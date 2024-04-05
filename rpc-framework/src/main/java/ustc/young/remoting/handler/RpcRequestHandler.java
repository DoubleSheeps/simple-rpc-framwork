package ustc.young.remoting.handler;

import lombok.extern.slf4j.Slf4j;
import ustc.young.factory.SingletonFactory;
import ustc.young.provider.ServiceProvider;
import ustc.young.provider.impl.ZkServiceProviderImpl;
import ustc.young.remoting.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:48
 **/
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler(){
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest,service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest,Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            result = method.invoke(service,rpcRequest.getParameters());
            log.info("成功调用service:[{}]的方法:[{}] ",rpcRequest.getInterfaceName(),rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("调用目标服务的方法失败",e);
        }
        return result;
    }

}
