package com.itrey.spring.beans.factory.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author chenfeng
 */
public class ServiceReplacerFactoryContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    class BaseBeanFactoryPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            //获取spring容器中所有的beanName
            String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
            if (beanDefinitionNames.length==0) {
                return;
            }

            prepare(beanFactory,beanDefinitionNames);

        }
        private void prepare(ConfigurableListableBeanFactory beanFactory, String[] beanDefinitionNames){

            Map<String,ServiceReplacerConfigInfo> serviceReplacerConfigs = new HashMap<>();
            for (String beanDefinitionName : beanDefinitionNames) {
                if(StringUtils.isEmpty(beanDefinitionName)){
                    continue;
                }
                BeanDefinition definition = beanFactory.getBeanDefinition(beanDefinitionName);
                if (definition==null) {
                    continue;
                }
                String className = definition.getBeanClassName();
                if (StringUtils.isEmpty(className)) {
                    continue;
                }

                resolveDefinition(beanFactory, beanDefinitionName,className,serviceReplacerConfigs);
            }

            //
            serviceReplacerConfigs.values().forEach(item->{
                String methodReplacerBeanName = item.getBeanName()+"-methodReplacer";
                BeanDefinitionRegistry factory = (BeanDefinitionRegistry)beanFactory;
                if (!factory.containsBeanDefinition(methodReplacerBeanName)) {
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(
                        ServiceMethodReplacer.class);
                    beanDefinitionBuilder.addPropertyValue("name",item.getBeanName());
                    beanDefinitionBuilder.addPropertyReference("replacer",item.getBeanName());
                    BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                    factory.registerBeanDefinition(methodReplacerBeanName,beanDefinition);
                }
                DefaultListableBeanFactory dd  = null;
                AbstractBeanDefinition db = (AbstractBeanDefinition)beanFactory.getBeanDefinition(item.getTargetBeanName());
                ReplaceOverride replaceOverride = new ReplaceOverride(item.getMethod().getName(), methodReplacerBeanName);
                for (Class<?> parameterType : item.getMethod().getParameterTypes()) {
                    replaceOverride.addTypeIdentifier(parameterType.getTypeName());
                }
                db.getMethodOverrides().addOverride(replaceOverride);
            });

        }

        private void resolveDefinition(ConfigurableListableBeanFactory beanFactory,
                                       String beanName,
                                       String className,
                                       Map<String,ServiceReplacerConfigInfo> serviceReplacerConfigs) {
            //找出bean对的类信息
            Class<?> clazz = ClassUtils.resolveClassName(className, beanFactory.getBeanClassLoader());

            //遍历方法找出需要进行替换的
            ReflectionUtils.doWithMethods(clazz, method -> {
                //获取方法的ServiceReplacer注解信息
                ServiceReplacer serviceReplacer = AnnotationUtils.getAnnotation(method, ServiceReplacer.class);

                //如果无ServiceReplacer注解直接返回
                if (Objects.isNull(serviceReplacer)) {
                    return;
                }

                String[] beanNames = serviceReplacer.targets();
                if (Objects.nonNull(beanNames)) {
                    for (String targetBeanName : beanNames) {
                        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(targetBeanName);
                        if (Objects.isNull(beanDefinition)){
                            continue;
                        }

                        if (serviceReplacerConfigs.containsKey(targetBeanName)) {
                            ServiceReplacerConfigInfo info = serviceReplacerConfigs.get(targetBeanName);
                            if (serviceReplacer.order()<info.getOrder()) {
                                return;
                            }
                        }
                        //如果匹配的话
                        ServiceReplacerConfigInfo info = ServiceReplacerConfigInfo.builder()
                            .targetBeanName(targetBeanName)
                            .beanName(beanName)
                            .method(method)
                            .order(serviceReplacer.order())
                            .build();
                        serviceReplacerConfigs.put(targetBeanName,info);
                    }
                }

            });

        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Assert.notNull(applicationContext, "Context must not be null");

        applicationContext.addBeanFactoryPostProcessor(
            new BaseBeanFactoryPostProcessor()
        );
    }
}
