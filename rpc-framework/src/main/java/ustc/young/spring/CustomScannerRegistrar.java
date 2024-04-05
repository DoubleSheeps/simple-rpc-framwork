package ustc.young.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;
import ustc.young.annotations.RpcScan;
import ustc.young.annotations.RpcService;

import java.util.Set;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 16:28
 **/
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final String SPRING_BEAN_BASE_PACKAGE = "ustc.young";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if(rpcScanAnnotationAttributes != null){
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if(rpcScanBasePackages.length==0){
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) importingClassMetadata).getIntrospectedClass().getPackage().getName()};
        }
        CustomScanner rpcServiceScanner = new CustomScanner(registry, RpcService.class);
        CustomScanner springBeanScanner = new CustomScanner(registry, Component.class);
        if(resourceLoader!=null){
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        Set<BeanDefinitionHolder> springBeans = springBeanScanner.doScan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeans.size());
        springBeans.forEach(beanDefinitionHolder->{
            log.info("springBeanScanner扫描到[{}]",beanDefinitionHolder.getBeanName());
        });
        Set<BeanDefinitionHolder> rpcServices = rpcServiceScanner.doScan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServices.size());
        rpcServices.forEach(beanDefinitionHolder->{
            log.info("rpcServiceScanner扫描到[{}]",beanDefinitionHolder.getBeanName());
        });
    }
}
