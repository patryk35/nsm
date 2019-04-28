package pdm.networkservicesmonitor.agent.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String beanId) {
        return applicationContext.getBean(beanId);
    }

    public static <T> T getBean(String beanId, Class<T> beanClass) {
        return applicationContext.getBean(beanId, beanClass);
    }

    public void removeExistingAndAddNewBean(String beanId, BeanDefinition newBeanObj) {

        AutowireCapableBeanFactory factory =
                applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        registry.removeBeanDefinition(beanId);

        //create newBeanObj through GenericBeanDefinition

        registry.registerBeanDefinition(beanId, newBeanObj);
    }
}