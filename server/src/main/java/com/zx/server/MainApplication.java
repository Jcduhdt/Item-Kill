package com.zx.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-10
 */
@SpringBootApplication
// 连接数据库的通用配置
@ImportResource(value = {"classpath:spring/spring-jdbc.xml"})
// 扫描mybatis的mapper
@MapperScan(basePackages = "com.zx.model.mapper")
// 开启定时任务
@EnableScheduling
public class MainApplication extends SpringBootServletInitializer {

    // 视频上说，因为该项目是用的外置tomcat跑的
    // 所以这里必须继承该类以及重写这个方法，才能启动SpringBoot项目
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MainApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class,args);
    }
}
