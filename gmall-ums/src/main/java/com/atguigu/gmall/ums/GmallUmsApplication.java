package com.atguigu.gmall.ums;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
@EnableDiscoveryClient//启用注册功能开关默认开启
@SpringBootApplication
@EnableFeignClients
@EnableSwagger2
@MapperScan("com.atguigu.gmall.ums.mapper")
public class GmallUmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUmsApplication.class, args);
    }

}
