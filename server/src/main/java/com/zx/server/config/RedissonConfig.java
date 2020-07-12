package com.zx.server.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-12
 * Redisson通用化配置
 */
@Configuration
public class RedissonConfig {

    @Autowired
    private Environment env;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(env.getProperty("redis.config.host"))
                .setPassword(env.getProperty("spring.redis.password"));
        RedissonClient client = Redisson.create(config);
        return client;
    }


}
