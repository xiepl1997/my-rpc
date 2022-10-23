package com.xpl.study.consumer;

import com.xpl.study.annotation.RPCConsumer;
import com.xpl.study.constans.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RPC消费者bean注入
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Slf4j
public class RpcConsumerPostProcessor implements BeanFactoryPostProcessor, BeanClassLoaderAware, ApplicationContextAware {

    private ConfigurableListableBeanFactory beanFactory;

    private ClassLoader classLoader;

    private ApplicationContext context;

    /**
     * linkedHashMap保证有序
     */
    private Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap<>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        this.beanFactory = configurableListableBeanFactory;
        // 遍历容器里的所有bean
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = definition.getBeanClassName();
            // 当用@Bean返回的类型是Object时，beanClassName是null
            if (beanClassName != null) {
                // 使用反射获取bean的class对象，注意classloader是容器加载bean的classloader
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseElement);
            }
        }

        // 重新注入到容器中
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        this.beanDefinitions.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("[RPC Starter] Spring context already has a bean named " + beanName
                    + ", please change @RPCConsumer field name.");
            }
            registry.registerBeanDefinition(beanName, beanDefinition);
            log.info("registered RPCConsumerBean \"{}\" in spring context.", beanName);
        });
    }

    /**
     * 动态修改被RPCConsumer注解的bean，改为代理类
     *
     * @param field
     */
    private void parseElement(Field field) {
        RPCConsumer annotation = AnnotationUtils.getAnnotation(field, RPCConsumer.class);
        if (null == annotation) {
            return;
        }

        // 构造新的factory bean的参数，hack成为自己的代理bean
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcConsumerBean.class);
        builder.setInitMethodName(Constants.INIT_METHOD);
        builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
        builder.addPropertyValue("interfaceClass", field.getType());
        builder.addPropertyValue("registryType", annotation.registryType());
        builder.addPropertyValue("registryAddress", annotation.registryAddress());

        BeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinitions.put(field.getName(), beanDefinition);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
