package ustc.young.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import ustc.young.annotations.RpcReference;
import ustc.young.annotations.RpcService;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.factory.SingletonFactory;
import ustc.young.provider.ServiceProvider;
import ustc.young.provider.impl.ZkServiceProviderImpl;
import ustc.young.proxy.RpcClientProxy;
import ustc.young.remoting.transport.RpcRequestTransport;
import ustc.young.utils.PropertiesFileUtil;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 16:59
 **/
@Component
@Slf4j
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;
    private final int nettyPort;
    private static final int DEFAULT_PORT=4321;

    public SpringBeanPostProcessor(){
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension(RpcRequestTransportEnum.NETTY.getName());
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        this.nettyPort =  properties!=null && properties.getProperty(RpcConfigEnum.NETTY_PORT.getPropertyValue())!=null
                ? Integer.parseInt(properties.getProperty(RpcConfigEnum.NETTY_PORT.getPropertyValue()))
                : DEFAULT_PORT;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("[{}]类使用了[{}]注解",bean.getClass().getName(),RpcService.class.getName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .transport(RpcRequestTransportEnum.NETTY.getName())
                    .service(bean)
                    .build();
            serviceProvider.publishService(rpcServiceConfig, this.nettyPort);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields){
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if(rpcReference!=null){
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .transport(RpcRequestTransportEnum.NETTY.getName())
                        .build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient,rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean,clientProxy);
                } catch (IllegalAccessException e) {
                    log.error("自动注册RpcReference失败,",e);
                }
            }
        }
        return bean;
    }

}
