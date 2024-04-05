package ustc.young.serviceImpl;

import ustc.young.Hello;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-02 17:58
 **/
public class HelloImpl2 implements Hello {
    @Override
    public String hello(String name) {
        return "helloImpl2 call hello, "+ name;
    }
}
