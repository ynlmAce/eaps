//package com.fq.yznu.eaps.config;
//
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FactoryBeanDebugProcessor implements BeanFactoryPostProcessor {
//
//    public FactoryBeanDebugProcessor() {
//        System.out.println("FactoryBeanDebugProcessor initialized");  // 检查类是否加载
//    }
//
//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        System.out.println("postProcessBeanFactory called");  // 确保方法被调用
//        for (String name : beanFactory.getBeanDefinitionNames()) {
//            BeanDefinition def = beanFactory.getBeanDefinition(name);
//            Object attr = def.getAttribute("factoryBeanObjectType");
//            if (attr != null) {
//                System.out.println(">>>>>>>>>>> factoryBeanObjectType found:");
//                System.out.println("Bean name: " + name);
//                System.out.println("Value type: " + attr.getClass());
//                System.out.println("Value: " + attr);
//            }
//        }
//    }
//}
