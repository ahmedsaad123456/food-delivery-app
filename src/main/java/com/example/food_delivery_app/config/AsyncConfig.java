package com.example.food_delivery_app.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/*
* This class configures a thread pool for @Async methods.
* It defines a ThreadPoolTaskExecutor bean named "orderExecutor"
* with specific settings for core pool size, max pool size, queue capacity, thread name prefix, and keep-alive seconds.
* It also sets a rejected execution handler to run tasks on the caller thread when the pool and queue are full.
* The @EnableAsync annotation enables asynchronous method execution in the application.
*
* */
@Configuration
@EnableAsync // turns on the machinery that makes @Async work (creates the proxies)
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "orderExecutor") // this exact name is what @Async("orderExecutor") refers to
    public ThreadPoolTaskExecutor orderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);      // threads that are always alive
        executor.setMaxPoolSize(8);       // upper limit, used ONLY when the queue is full
        executor.setQueueCapacity(50);    // tasks allowed to wait; bounded on purpose
        executor.setThreadNamePrefix("orderExecutor-"); // thread names: orderExecutor-1, -2 ...
        executor.setKeepAliveSeconds(60); // extra threads (above core) die after 60s idle

        // When pool + queue are full: run the task on the CALLER thread instead of throwing.
        // This slows the caller down, which naturally pushes back on whoever is flooding us.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Propagate the SecurityContext and MDC from the caller thread to the worker thread.
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());

        // On shutdown: finish what is already queued (up to 30s) instead of dropping it.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        return executor; // Spring calls initialize() for us because this is a bean
    }

    // Used for @Async methods that return void: nobody can catch their exception, so we log it.
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async method {} failed with params {}", method.toGenericString(), params, ex);
    }
}