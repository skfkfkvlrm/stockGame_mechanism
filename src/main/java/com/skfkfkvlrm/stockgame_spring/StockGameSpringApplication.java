package com.skfkfkvlrm.stockgame_spring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(value = "com.skfkfkvlrm.stockgame_spring.domain", annotationClass = org.apache.ibatis.annotations.Mapper.class)
@EnableScheduling
public class StockGameSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockGameSpringApplication.class, args);
    }

}
