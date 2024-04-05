package ustc.young.annotations;

import org.springframework.context.annotation.Import;
import ustc.young.spring.CustomScannerRegistrar;

import java.lang.annotation.*;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 16:33
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {
    String[] basePackage();
}
