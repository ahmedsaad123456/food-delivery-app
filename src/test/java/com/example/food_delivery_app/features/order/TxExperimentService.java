package com.example.food_delivery_app.features.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
public class TxExperimentService {

    private final AsyncTxProbe probe;


    // Constructor injection of AsyncTxProbe
    public TxExperimentService(AsyncTxProbe probe) { this.probe = probe; }

    @Transactional
    public boolean startTransactionThenCallAsync() {
        // Check if the transaction is active in the caller thread
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("[{}] caller: transaction active = {}", Thread.currentThread().getName(), active);
        // Call the async method to check if the transaction is active in the async thread
        probe.check(); // goes to another thread
        return active;
    }
}