package com.xpl.study.consumer;

import com.xpl.study.commons.RpcProperties;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * starter封装
 *
 * @author peiliang xie
 * @date 2022/10/24
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcConsumerAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor rpcConsumerPostProcess() {
        return new RpcConsumerPostProcessor();
    }
}
