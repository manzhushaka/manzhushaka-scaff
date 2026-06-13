package com.manzhushaka.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定义 ManzhushakaApplication。
 */
@SpringBootApplication(scanBasePackages = "com.manzhushaka")
@MapperScan("com.manzhushaka.db.system.mapper")
@EnableAsync
@EnableScheduling
public class ManzhushakaApplication {

    /**
     * 启动应用。
     *
     * @param args args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ManzhushakaApplication.class, args);
    }
}
