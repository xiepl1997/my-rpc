package com.xpl.study.provider;

import com.xpl.study.commons.ProviderUtils;
import com.xpl.study.protocol.RpcRequest;
import com.xpl.study.protocol.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;

import java.util.Map;

/**
 * Rpc服务处理器
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcProvider.submit(() -> {
            log.debug("Receive request {}", rpcRequest.getRequestId());
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcRequest.getRequestId());
            try {
                Object result = handle(rpcRequest);
                response.setResult(result);
            } catch (Throwable throwable) {
                response.setError(throwable.toString());
                log.error("RPC Server handle request error");
            }
            channelHandlerContext.writeAndFlush(response).addListener(
                (ChannelFutureListener)channelFuture ->
                    log.debug("Send response for request " + rpcRequest.getRequestId()));
        });
    }

    private Object handle(RpcRequest request) throws Throwable {
        String providerKey = ProviderUtils.makeKey(request.getClassName(), request.getServiceVersion());
        Object providerBean = handlerMap.get(providerKey);

        if (null == providerBean) {
            throw new RuntimeException(String.format("provider not exist: %s:%s", request.getClassName(),
                request.getMethodName()));
        }

        Class<?> providerClass = providerBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        log.debug(providerClass.getName());
        log.debug(methodName);

        for (Class<?> parameterType : parameterTypes) {
            log.debug(parameterType.getName());
        }
        for (Object parameter : parameters) {
            log.debug(parameter.toString());
        }

        // 使用CGLib创建一个服务生产者代理对象，调用消费者指定的方法
        FastClass providerFastClass = FastClass.create(providerClass);
        int methodIndex = providerFastClass.getIndex(methodName, parameterTypes);
        return providerFastClass.invoke(methodIndex, providerBean, parameters);
    }
}
