package ustc.young.serviceImpl;

import ustc.young.Hello;
import ustc.young.annotations.RpcService;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 17:20
 **/
@RpcService(version = "1.0",group = "test")
public class HelloImpl implements Hello {
    @Override
    public String hello(String name) {
        return "helloImpl call hello, "+ name;
    }
}
