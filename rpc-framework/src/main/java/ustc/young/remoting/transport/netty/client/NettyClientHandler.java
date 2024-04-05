package ustc.young.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import ustc.young.enums.RpcMessageTypeEnum;
import ustc.young.enums.RpcResponseStatusEnum;
import ustc.young.factory.SingletonFactory;
import ustc.young.remoting.dto.RpcMessage;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;

import java.net.InetSocketAddress;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 15:53
 **/
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final ResponseFuture responseFuture;
    private final ChannelProvider channelProvider;

    public NettyClientHandler(){
        this.responseFuture = SingletonFactory.getInstance(ResponseFuture.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcMessage<Object> rpcMessage = (RpcMessage<Object>) msg;
            log.info("客户端收到消息：{}",rpcMessage.toString());
            if (rpcMessage.getType() == RpcMessageTypeEnum.PONG.getType()) {
                log.info("heart [{}]", rpcMessage.getData());
            } else if(rpcMessage.getType()==RpcMessageTypeEnum.RESPONSE.getType()){
//                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
//                ctx.channel().attr(key).set(rpcResponse);
//                ctx.channel().close();
                responseFuture.complete((RpcResponse<Object>) rpcMessage.getData());
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent)evt).state();
            if (state==IdleState.WRITER_IDLE){
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = channelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = RpcMessage.builder()
                        .type(RpcMessageTypeEnum.PING.getType())
                        .data("Ping").build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端出现异常：",cause);
        ctx.close();
    }
}
