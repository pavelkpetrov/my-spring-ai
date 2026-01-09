package com.coherentsolutions.homework.week1.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    public <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public Object getBean(String beanName) {
        return context.getBean(beanName);
    }
}
