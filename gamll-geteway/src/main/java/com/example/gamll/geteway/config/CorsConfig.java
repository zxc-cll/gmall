package com.example.gamll.geteway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author zstars
 * @create 2021-09-01 11:55
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){

        CorsConfiguration configuration = new CorsConfiguration();
        //允许哪些域名跨域访问  * 代表所有的域名可以访问，但是不安全 而且不能携带cookie
        configuration.addAllowedOrigin("http://manager.gmall.com");
        configuration.addAllowedOrigin("http://localhost:1000");
        configuration.addAllowedOrigin("http://127.0.0.1:1000");
        //允许哪些请求方法可以跨域访问
        configuration.addAllowedMethod("*");
        //允许携带哪些头信息跨域访问
        configuration.addAllowedHeader("*");
        //是否携带cookie
        configuration.setAllowCredentials(true);

        //初始化一个cors配置源的对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",configuration);
        return new CorsWebFilter(configurationSource);
    }
}
