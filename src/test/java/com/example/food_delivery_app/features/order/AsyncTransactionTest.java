package com.example.food_delivery_app.features.order;

import com.example.food_delivery_app.inventory.concurrency.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.awaitility.Awaitility;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AsyncTransactionTest extends AbstractIntegrationTest {

    @Autowired
    TxExperimentService service;
    @Autowired AsyncTxProbe probe;

    @Test
    void transaction_does_not_cross_into_the_async_thread() {
        // The caller thread has a transaction,
        // but the async thread does not.
        // This is because @Async runs in a different thread, and Spring's transaction management is thread-bound.
        // The async thread does not inherit the transaction from the caller thread.
        boolean callerActive = service.startTransactionThenCallAsync();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> probe.activeInAsync() != null);

        assertThat(callerActive).isTrue();           // the caller really had a transaction
        assertThat(probe.activeInAsync()).isFalse(); // the async thread did NOT
    }
}