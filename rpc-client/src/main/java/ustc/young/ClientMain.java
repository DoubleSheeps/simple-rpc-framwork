package ustc.young;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ustc.young.annotations.RpcScan;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 18:04
 **/
@RpcScan(basePackage = {"usct.young"})
public class ClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}