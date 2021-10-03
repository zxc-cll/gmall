package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author zstars
 * @create 2021-09-28 21:48
 */
@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct  //类似于<bean class="" init-method="">
    //@PreDestroy   //相当于<bean class="" destroy-method="">
    void init(){
        this.rabbitTemplate.setConfirmCallback(( correlationData,  ack,   cause) ->{
            if (ack) {
                System.out.println("消息到达队列=====");
            }else {
                System.out.println("消息没有到达队列----------"+cause);
            }
        });

        this.rabbitTemplate.setReturnCallback(( message,  replyCode,  replyText,  exchange,  routingKey) ->{
            log.error("消息没有到达队列：交换机，{}，路由键：{}，消息内容：{}，状态码：{}，状态说明：{}",exchange,routingKey,new String(message.getBody()));
        });

    }
}
