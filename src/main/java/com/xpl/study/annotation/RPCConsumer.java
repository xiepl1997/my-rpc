package com.xpl.study.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC消费者注解
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired
public @interface RPCConsumer {

    /**
     * @see
     * @return
     */
    String serviceVersion() default "1.0.0";

    String registryType() default "zookeeper";

    String registryAddress() default "127.0.0.1:2181";
}
