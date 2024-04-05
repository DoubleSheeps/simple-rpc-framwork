package ustc.young.registry.zookeeper;


import org.junit.jupiter.api.Test;
import ustc.young.enums.RpcRequestTransportEnum;
import ustc.young.registry.ServiceDiscovery;
import ustc.young.registry.ServiceRegistry;
import ustc.young.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 16:43
 **/
public class ZkServiceDiscoveryImplTest {

    @Test
    public void should_register_service_successful_and_lookup_service_by_service_name() {
        ServiceRegistry zkServiceRegistry = new ZkServiceRegistryImpl();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1",9345);
        zkServiceRegistry.registerService("Demo2test2version1",givenInetSocketAddress,"test");
        InetSocketAddress givenInetSocketAddress1 = new InetSocketAddress("127.0.0.1",9346);
        zkServiceRegistry.registerService("Demo2test2version1",givenInetSocketAddress1,"test");
        ServiceDiscovery zkServiceDiscovery = new ZkServiceDiscoveryImpl();
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName("Demo2")
                .requestId(UUID.randomUUID().toString())
                .group("test2")
                .parameters(new Integer[]{1,2})
                .version("version1")
                .build();
        for (int i=0;i<10;i++) {
            InetSocketAddress acquiredInetSocketAddress = zkServiceDiscovery.lookupService(rpcRequest, RpcRequestTransportEnum.SOCKET.getName());
            System.out.println(acquiredInetSocketAddress.toString());
        }
//        assertEquals(givenInetSocketAddress.toString(),acquiredInetSocketAddress.toString());
    }
}