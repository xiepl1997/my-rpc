package com.xpl.study.protocol;

import com.xpl.study.protocol.serialize.HessianSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Rpc编码器
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class RpcEncoder extends MessageToByteEncoder {

    public RpcEncoder() {}

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] data = HessianSerialize.serialize(o);
        assert data != null;
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
