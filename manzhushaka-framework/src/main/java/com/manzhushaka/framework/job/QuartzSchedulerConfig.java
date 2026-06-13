package com.manzhushaka.framework.job;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * 定义 QuartzSchedulerConfig。
 */
@Configuration
public class QuartzSchedulerConfig {

    /**
     * 执行 scheduler Factory Bean Customizer 逻辑。
     *
     * @param beanFactory beanFactory 参数
     * @return 处理结果
     */
    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(AutowireCapableBeanFactory beanFactory) {
        return schedulerFactoryBean -> schedulerFactoryBean.setJobFactory(new AutowireQuartzJobFactory(beanFactory));
    }

    private static final class AutowireQuartzJobFactory extends SpringBeanJobFactory {
        private final AutowireCapableBeanFactory beanFactory;

        /**
         * 执行 Autowire Quartz Job Factory 逻辑。
         *
         * @param beanFactory beanFactory 参数
         * @return 处理结果
         */
        private AutowireQuartzJobFactory(AutowireCapableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        /**
         * 创建 create Job Instance 数据。
         *
         * @param bundle bundle 参数
         * @return 创建结果
         */
        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }
}
