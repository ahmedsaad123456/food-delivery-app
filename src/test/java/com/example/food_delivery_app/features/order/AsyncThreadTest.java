package com.example.food_delivery_app.features.order;

import com.example.food_delivery_app.inventory.concurrency.AbstractIntegrationTest;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.awaitility.Awaitility;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest // needed, otherwise @Autowired stays null
class AsyncThreadTest extends AbstractIntegrationTest {

    @Autowired
    AsyncProbe probe;

    @Test
    void async_method_runs_on_the_orderExecutor_not_on_the_caller() {
        String callerThread = Thread.currentThread().getName();

        probe.recordAsync(); // returns immediately, the work happens elsewhere

        // Awaitility keeps checking until the async work is done (max 5s); no Thread.sleep guesses
        // Awaitility keeps asking about the condition until it is true or the timeout is reached. This avoids flaky tests that might fail if the async work takes longer than expected.
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> probe.threadName() != null);

        assertThat(probe.threadName()).startsWith("orderExecutor-");
        assertThat(probe.threadName()).isNotEqualTo(callerThread);
    }


    // Test that the SecurityContext and MDC are propagated to the async thread, and that they do not leak to the next task.
    @Test
    void context_is_propagated_into_the_async_thread() throws Exception {
        // Set up the SecurityContext and MDC in the caller thread
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ahmed", "n/a", List.of()));
        MDC.put("requestId", "r-42");
        try {
            // Call the async method that reads the context and returns it as a string
            String seen = probe.readContext().get(5, TimeUnit.SECONDS); // wait for the async result
            assertThat(seen).isEqualTo("ahmed|r-42");
        } finally {
            // Clean up the SecurityContext and MDC in the caller thread to avoid leaking to other tests
            SecurityContextHolder.clearContext();
            MDC.clear();
        }
    }

    // Test that the SecurityContext does not leak to the next task, even if the caller thread has cleared it.
    @Test
    void context_does_not_leak_to_the_next_task() throws Exception {

        // Set up the SecurityContext in the caller thread
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ahmed", "n/a", List.of()));
        probe.readContext().get(5, TimeUnit.SECONDS);
        SecurityContextHolder.clearContext(); // now the caller has NO user

        // many tasks so that every worker thread is likely reused; none may still see "ahmed"
        for (int i = 0; i < 20; i++) {
            assertThat(probe.readContext().get(5, TimeUnit.SECONDS)).startsWith("none");
        }
    }
}