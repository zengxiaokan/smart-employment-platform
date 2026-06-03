package com.itzk.SmartEmploymentPlatform.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.database}")
    private int database;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);
        serverConfig.setPassword(password);
        serverConfig.setDatabase(database);
        //启用 TCP KeepAlive 后，操作系统会定时发送很小的保活包，让中间设备认为连接仍然活跃，从而保持连接不被回收。
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(3))
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .connectTimeout(Duration.ofSeconds(3))
                                .keepAlive(SocketOptions.KeepAliveOptions.builder()
                                        .enable()
                                        .idle(Duration.ofSeconds(60))
                                        .interval(Duration.ofSeconds(30))
                                        .count(3)
                                        .build())
                                .build())
                        .build())
                .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
