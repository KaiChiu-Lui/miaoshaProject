package com.miaoshaproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServer6001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer6001.class,args);
        System.out.println("eureka服务器6001启动成功");
    }
}
