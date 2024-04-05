package ustc.young.remoting.transport.netty.constants;

/**
 * @author YoungSheep
 * @description
 * @date 2024-04-01 15:27
 **/
public class RpcConstants {
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
}
