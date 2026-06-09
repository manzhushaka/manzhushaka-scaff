package com.manzhushaka.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.manzhushaka")
@MapperScan("com.manzhushaka.db.system.mapper")
public class ManzhushakaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManzhushakaApplication.class, args);
    }
}
