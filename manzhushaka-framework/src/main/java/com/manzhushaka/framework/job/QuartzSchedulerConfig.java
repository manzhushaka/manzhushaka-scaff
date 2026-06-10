package com.manzhushaka.framework.job;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzSchedulerConfig {

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(AutowireCapableBeanFactory beanFactory) {
        return schedulerFactoryBean -> schedulerFactoryBean.setJobFactory(new AutowireQuartzJobFactory(beanFactory));
    }

    private static final class AutowireQuartzJobFactory extends SpringBeanJobFactory {
        private final AutowireCapableBeanFactory beanFactory;

        private AutowireQuartzJobFactory(AutowireCapableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }
}
