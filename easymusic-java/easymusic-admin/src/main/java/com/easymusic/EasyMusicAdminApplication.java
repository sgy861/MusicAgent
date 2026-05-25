package com.easymusic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.easymusic"})
@MapperScan(basePackages = {"com.easymusic.mappers"})
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class EasyMusicAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyMusicAdminApplication.class, args);
    }
}