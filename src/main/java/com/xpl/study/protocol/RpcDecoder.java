package com.xpl.study.protocol;

import com.xpl.study.protocol.serialize.HessianSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Rpc解码器
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class RpcDecoder extends ByteToMessageDecoder {

    public RpcDecoder() {}

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        Object obj = HessianSerialize.deserialize(data);
        assert obj != null;
        list.add(obj);
    }
}
