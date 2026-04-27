package com.shortvideo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.shortvideo.mapper")
@EnableScheduling
@ComponentScan(basePackages = "com.shortvideo") // 扫描所有包，包括 admin 控制器
public class ShortVideoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortVideoApplication.class, args);
    }
}