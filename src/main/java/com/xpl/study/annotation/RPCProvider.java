package com.xpl.study.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者注解
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Retention(RetentionPolicy.RUNTIME) // 运行时
@Target(ElementType.TYPE)           // 注解class
@Component                          // 被spring加载
public @interface RPCProvider {

    Class<?> serviceInterface() default Object.class; // 设置接口

    String serviceVersion() default "1.0.0";          // 版本
}
