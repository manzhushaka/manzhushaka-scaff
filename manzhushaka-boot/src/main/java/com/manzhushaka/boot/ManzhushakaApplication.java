package com.manzhushaka.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.manzhushaka")
@MapperScan("com.manzhushaka.db.system.mapper")
@EnableAsync
@EnableScheduling
public class ManzhushakaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManzhushakaApplication.class, args);
    }
}
