package ustc.young.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ustc.young.remoting.transport.netty.coder.NettyDecoder;
import ustc.young.remoting.transport.netty.coder.NettyEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-03 15:15
 **/
@Slf4j
public class ChannelProvider {
    private final Bootstrap bootstrap;
    private final ConcurrentHashMap<String, Channel> channelConcurrentHashMap;

    public ChannelProvider(){
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // If no data is sent to the server within 5 seconds, a heartbeat request is sent
                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        socketChannel.pipeline().addLast(new NettyDecoder());
                        socketChannel.pipeline().addLast(new NettyEncoder());
                        socketChannel.pipeline().addLast(new NettyClientHandler());

                    }
                });
        channelConcurrentHashMap = new ConcurrentHashMap<>();
    }

    @SneakyThrows
    private Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener)future -> {
            if(future.isSuccess()){
                completableFuture.complete(future.channel());
                log.info("Netty客户端[{}]连接成功",inetSocketAddress.toString());
            }else {
                throw new IllegalStateException("Netty客户端连接失败");
            }
        });
        return completableFuture.get();
    }

    public Channel get(InetSocketAddress inetSocketAddress)  {
        if(channelConcurrentHashMap.containsKey(inetSocketAddress.toString())){
            Channel channel = channelConcurrentHashMap.get(inetSocketAddress.toString());
            if(channel!=null&&channel.isActive()){
                return channel;
            }else {
                channelConcurrentHashMap.remove(inetSocketAddress.toString());
            }
        }
        Channel channel = doConnect(inetSocketAddress);
        channelConcurrentHashMap.put(inetSocketAddress.toString(),channel);
        return channel;
    }
}
