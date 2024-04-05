package ustc.young.remoting.transport.netty.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import ustc.young.enums.SerializeEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.remoting.dto.RpcMessage;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.serialize.Serializer;

import java.util.List;

/**
 * @author YoungSheep
 * @description Netty解码器
 * @date 2024-04-01 15:10
 **/
@Slf4j
public class NettyDecoder extends ByteToMessageDecoder {

    private static final int BODY_MARK_LENGTH = 4;
    private static final int HEAD_LENGTH = 1;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes()>=BODY_MARK_LENGTH+HEAD_LENGTH){
            //标记当前readIndex的位置，以便后面重置readIndex 的时候使用
            byteBuf.markReaderIndex();
            int dataLength = byteBuf.readInt();
            if(dataLength<HEAD_LENGTH||byteBuf.readableBytes()<HEAD_LENGTH){
                log.error("传输的数据长度或缓存中的字节长度不合法");
                return;
            }
            //如果可读字节数小于消息长度的话，说明是不完整的消息，重置readInde
            if(byteBuf.readableBytes()<dataLength){
                byteBuf.resetReaderIndex();
                return;
            }
            byte serializeCode = byteBuf.readByte();
            String serializeName = SerializeEnum.getName(serializeCode);
            if(serializeName==null){
                log.error("传输数据的序列化协议字段不合法");
                return;
            }
//            byte type = byteBuf.readByte();
//            if(type!=(byte) 1&&type!=(byte) 2&&type!=(byte) 3&&type!=(byte) 4){
//                log.error("传输数据的消息类型字段不合法");
//                return;
//            }
            byte[] body = new byte[dataLength-HEAD_LENGTH];
            byteBuf.readBytes(body);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializeName);
//            Class<?> clazz = (type==(byte) 1 ? RpcRequest.class : RpcResponse.class);
            Class<?> clazz = RpcMessage.class;
            Object obj = serializer.deserialize(body, clazz);
            list.add(obj);
            log.info("成功将缓存中的字节转换成对象：{}",obj.toString());
        }
    }
}
