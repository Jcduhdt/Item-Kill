package com.zx.server.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-12
 * 自定义ZooKeeper配置
 */
@Configuration
public class ZooKeeperConfig {

    @Autowired
    private Environment env;

    /**
     * 自定义注入ZooKeeper客户端操作实例
     * @return
     */
    @Bean
    public CuratorFramework curatorFramework(){
        CuratorFramework curatorFramework= CuratorFrameworkFactory.builder()
                .connectString(env.getProperty("zk.host"))
                .namespace(env.getProperty("zk.namespace"))
                //重试策略
                .retryPolicy(new RetryNTimes(5,1000))
                .build();
        curatorFramework.start();
        return curatorFramework;
    }


}
