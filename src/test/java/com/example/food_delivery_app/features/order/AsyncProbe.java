package com.example.food_delivery_app.features.order;

import org.jboss.logging.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AsyncProbe {

    private volatile String threadName; // volatile: written by the pool thread, read by the test thread


    // To test that the async method runs on the orderExecutor thread pool, not on the caller thread.
    @Async("orderExecutor")
    public void recordAsync() {
        this.threadName = Thread.currentThread().getName();
    }


    // To test that the SecurityContext and MDC are propagated to the async thread, we read them and return them as a string.
    @Async("orderExecutor")
    public CompletableFuture<String> readContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String user = auth == null ? "none" : auth.getName();
        return CompletableFuture.completedFuture(user + "|" + MDC.get("requestId"));
    }

    public String threadName() { return threadName; }
}