package ustc.young.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import ustc.young.enums.*;
import ustc.young.extension.ExtensionLoader;
import ustc.young.factory.SingletonFactory;
import ustc.young.registry.ServiceDiscovery;
import ustc.young.remoting.dto.RpcMessage;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.remoting.transport.RpcRequestTransport;
import ustc.young.remoting.transport.netty.coder.NettyDecoder;
import ustc.young.remoting.transport.netty.coder.NettyEncoder;
import ustc.young.utils.PropertiesFileUtil;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 15:41
 **/
@Slf4j
public class NettyClient implements RpcRequestTransport {
//    private static final Bootstrap b;

    private final ServiceDiscovery serviceDiscovery;

    private final ResponseFuture responseFuture;

    private final ChannelProvider channelProvider;


    public NettyClient(){
        this.responseFuture = SingletonFactory.getInstance(ResponseFuture.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String serviceDiscoveryName = properties!=null && properties.getProperty(RpcConfigEnum.SERVICE_DISCOVERY.getPropertyValue())!=null
                ? properties.getProperty(RpcConfigEnum.SERVICE_DISCOVERY.getPropertyValue())
                : DefaultConfigEnum.DEFAULT_SERVICE_DISCOVERY.getName();
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(serviceDiscoveryName);
    }

//    static {
//        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
//        b = new Bootstrap();
//        b.group(eventLoopGroup)
//                .channel(NioSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.INFO))
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    protected void initChannel(SocketChannel socketChannel) throws Exception {
//                        // If no data is sent to the server within 5 seconds, a heartbeat request is sent
//                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
//                        socketChannel.pipeline().addLast(new NettyDecoder());
//                        socketChannel.pipeline().addLast(new NettyEncoder());
//                        socketChannel.pipeline().addLast(new NettyClientHandler());
//
//                    }
//                });
//    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest, RpcRequestTransportEnum.NETTY.getName());
        //利用CompletableFuture异步直接返回
        CompletableFuture<RpcResponse<Object>> completableFuture = new CompletableFuture<>();
//            ChannelFuture future = b.connect(inetSocketAddress).sync();
//            log.info("客户端连接成功");
//            Channel futureChannel = future.channel();
        Channel futureChannel = channelProvider.get(inetSocketAddress);
        if(futureChannel.isActive()){
            responseFuture.put(rpcRequest.getRequestId(),completableFuture);
            RpcMessage rpcMessage = RpcMessage.builder()
                    .type(RpcMessageTypeEnum.REQUEST.getType())
                    .data(rpcRequest).build();
            futureChannel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener)future1 -> {
                if(future1.isSuccess()){
                    log.info("发送成功：{}",rpcMessage.toString());
                }else {
                    future1.channel().close();
                    completableFuture.completeExceptionally(future1.cause());
                    log.error("发送失败，原因是:",future1.cause());
                }
            });
            //原方案：阻塞等待收到消息加入AttributeKey并关闭通道后再取出值
//                futureChannel.closeFuture().sync();
//                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
//                return futureChannel.attr(key).get();
        }
        else {
            throw new RuntimeException();
        }
        return completableFuture;
    }

}
