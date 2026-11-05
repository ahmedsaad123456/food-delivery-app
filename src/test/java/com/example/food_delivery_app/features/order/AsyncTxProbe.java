package com.example.food_delivery_app.features.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
public class AsyncTxProbe {

    private volatile Boolean activeInAsync; // visible across threads

    @Async("orderExecutor")
    public void check() {
        // Check if the transaction is active in the async thread
        activeInAsync = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("[{}] inside @Async: transaction active = {}", Thread.currentThread().getName(), activeInAsync);
    }

    public Boolean activeInAsync() { return activeInAsync; }
}