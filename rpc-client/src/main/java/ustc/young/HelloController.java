package ustc.young;

import org.springframework.stereotype.Component;
import ustc.young.annotations.RpcReference;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 18:04
 **/
@Component
public class HelloController {

    @RpcReference(version = "1.0",group = "test")
    private Hello hello;

    public void test() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println(hello.hello("young"));
        }
    }
}
