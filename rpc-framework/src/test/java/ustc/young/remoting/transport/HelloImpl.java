package ustc.young.remoting.transport;

import ustc.young.remoting.transport.Hello;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 14:49
 **/
public class HelloImpl implements Hello {
    @Override
    public String hello(String name) {
        return "test hello, "+name;
    }
}
