package com.light.translate.communicate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean("makeExecutor")
    public ThreadPoolTaskExecutor makeSentenceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);      // 核心线程数
        executor.setMaxPoolSize(3);       // 最大线程数（并发限制为3）
        executor.setQueueCapacity(100);   // 队列容量，超过就排队
        executor.setThreadNamePrefix("makeSentence-");
        executor.initialize();
        return executor;
    }
    @Bean("serialExecutor")
    public Executor serialExecutor() {
        return Executors.newSingleThreadExecutor(); // 串行执行任务
    }

}

