package com.itzk.SmartEmploymentPlatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@MapperScan("com.itzk.SmartEmploymentPlatform.mapper")
@EnableScheduling
public class SmartEmploymentPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartEmploymentPlatformApplication.class, args);
    }


}

