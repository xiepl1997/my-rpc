package com.xpl.study.consumer;

import com.xpl.study.protocol.RpcRequest;
import com.xpl.study.protocol.RpcResponse;
import com.xpl.study.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 动态代理实现类
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Slf4j
public class RpcInvokeHandler<T> implements InvocationHandler {

    private static final String EQUALS = "equals";

    private static final String HASH_CODE = "hashCode";

    private static final String TO_STRING = "toString";

    private String serviceVersion;

    private ServiceRegistry serviceRegistry;

    public RpcInvokeHandler() {}

    public RpcInvokeHandler(String serviceVersion, ServiceRegistry serviceRegistry) {
        this.serviceVersion = serviceVersion;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if (EQUALS.equals(name)) {
                return proxy == args[0];
            } else if (HASH_CODE.equals(name)) {
                return System.identityHashCode(proxy);
            } else if (TO_STRING.equals(name)) {
                return proxy.getClass().getName() + "@" +
                    Integer.toHexString(System.identityHashCode(proxy)) +
                    ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        // 通过netty向service provider发起网络请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setServiceVersion(this.serviceVersion);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        RpcConsumer rpcConsumer = new RpcConsumer(this.serviceRegistry);
        RpcResponse rpcResponse = rpcConsumer.sendRequest(request);
        if (rpcResponse != null) {
            return rpcResponse.getResult();
        } else {
            throw new RuntimeException("consumer rpc fail, response is null");
        }
    }
}
