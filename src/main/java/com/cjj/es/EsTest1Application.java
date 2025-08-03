package com.cjj.es;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(exclude = {KafkaAutoConfiguration.class})
@MapperScan("com.cjj.es.mapper")
public class EsTest1Application {

    public static void main(String[] args) {
        SpringApplication.run(EsTest1Application.class, args);
    }

}
