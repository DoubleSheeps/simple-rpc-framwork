package ustc.young.remoting.transport.netty.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ustc.young.enums.DefaultConfigEnum;
import ustc.young.enums.RpcConfigEnum;
import ustc.young.enums.SerializeEnum;
import ustc.young.extension.ExtensionLoader;
import ustc.young.remoting.dto.RpcRequest;
import ustc.young.remoting.dto.RpcResponse;
import ustc.young.remoting.transport.netty.constants.RpcConstants;
import ustc.young.serialize.Serializer;
import ustc.young.utils.PropertiesFileUtil;

import java.util.Properties;

/**
 * @author YoungSheep
 * @description Netty编码器
 * @date 2024-04-01 14:27
 **/
public class NettyEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        Properties properties = PropertiesFileUtil.getPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String serializerName =  properties!=null && properties.getProperty(RpcConfigEnum.SERIALIZER.getPropertyValue())!=null
                ? properties.getProperty(RpcConfigEnum.SERIALIZER.getPropertyValue())
                : DefaultConfigEnum.DEFAULT_SERIALIZER.getName();
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerName);
        byte[] body = serializer.serialize(o);
        int dataLength = body.length+1;
        byteBuf.writeInt(dataLength);
        byteBuf.writeByte(SerializeEnum.KRYO.getCode());
//        if(o instanceof RpcRequest){
//            byteBuf.writeByte(RpcConstants.REQUEST_TYPE);
//        }else if (o instanceof RpcResponse){
//            byteBuf.writeByte(RpcConstants.RESPONSE_TYPE);
//        }
        byteBuf.writeBytes(body);
    }
}
