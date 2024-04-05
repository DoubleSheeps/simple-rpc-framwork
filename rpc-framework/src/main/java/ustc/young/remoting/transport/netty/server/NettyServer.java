package ustc.young.remoting.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ustc.young.config.CustomShutdownHook;
import ustc.young.config.RpcServiceConfig;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.enums.SerializeEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.factory.SingletonFactory;
import ustc.young.provider.ServiceProvider;
import ustc.young.provider.impl.ZkServiceProviderImpl;
import ustc.young.remoting.transport.netty.coder.NettyDecoder;
import ustc.young.remoting.transport.netty.coder.NettyEncoder;
import ustc.young.serialize.Serializer;
import ustc.young.serialize.kryo.KryoSerializer;
import ustc.young.utils.PropertiesFileUtil;

import java.util.Properties;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 13:41
 **/
@Slf4j
@Component
public class NettyServer {
    private final int port;
    private static final int DEFAULT_PORT=4321;

    private final ServiceProvider serviceProvider;

    public NettyServer(){
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        this.port =  properties!=null && properties.getProperty(RpcConfigEnum.NETTY_PORT.getPropertyValue())!=null
                ? Integer.parseInt(properties.getProperty(RpcConfigEnum.NETTY_PORT.getPropertyValue()))
                : DEFAULT_PORT;
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig,port);
    }

    public void run(){
        CustomShutdownHook.getCustomShutdownHook().clearAll(port);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyDecoder());
                            socketChannel.pipeline().addLast(new NettyEncoder());
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("netty服务器启动失败");
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
