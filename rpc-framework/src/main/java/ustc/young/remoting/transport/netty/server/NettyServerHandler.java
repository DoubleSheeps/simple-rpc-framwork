package ustc.young.remoting.transport.netty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import ustc.young.enums.RpcMessageTypeEnum;
import ustc.young.factory.SingletonFactory;
import ustc.young.remoting.dto.RpcMessage;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.remoting.handler.RpcRequestHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 14:19
 **/
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);
    private final RpcRequestHandler rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof RpcMessage) {
                RpcMessage<Object> rpcMessage = (RpcMessage) msg;
                RpcMessage<Object> rpcResponseRpcMessage = null;
                if (rpcMessage.getType() == RpcMessageTypeEnum.REQUEST.getType()) {
                    log.info("服务器第{}次收到消息：{}", atomicInteger.getAndIncrement(), rpcMessage.toString());
                    RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    rpcResponseRpcMessage = RpcMessage.builder()
                            .type(RpcMessageTypeEnum.RESPONSE.getType())
                            .data(RpcResponse.success(result, rpcRequest.getRequestId())).build();
                } else if (rpcMessage.getType() == RpcMessageTypeEnum.PING.getType()) {
                    log.info("服务器收到客户端[{}]的Ping消息", ctx.channel().remoteAddress());
                    rpcResponseRpcMessage = RpcMessage.builder()
                            .type(RpcMessageTypeEnum.PONG.getType())
                            .data("Pong").build();
                }
                ChannelFuture f = ctx.writeAndFlush(rpcResponseRpcMessage);
                f.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent)evt).state();
            if(state == IdleState.READER_IDLE){
                log.info("检测到服务器读空闲，客户端已下线，关闭通道");
                ctx.close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务器出现异常：{}",cause);
        ctx.close();
    }
}
