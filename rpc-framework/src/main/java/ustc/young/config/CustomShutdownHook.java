package ustc.young.config;

import lombok.extern.slf4j.Slf4j;
import ustc.young.registry.zookeeper.util.CuratorUtils;
import ustc.young.utils.threadpool.ThreadPoolFactoryUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 16:44
 **/
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll(int port){
        log.info("添加停止时自动关闭当前所有已注册服务的hook");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),port);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(),inetSocketAddress);
            } catch (UnknownHostException e) {

            }
            ThreadPoolFactoryUtil.shutdownAllThreadPool();
        }));
    }

}
