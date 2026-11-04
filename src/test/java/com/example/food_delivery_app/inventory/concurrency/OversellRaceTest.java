package com.example.food_delivery_app.inventory.concurrency;

import com.example.food_delivery_app.inventory.service.LabInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

// The pool must be bigger than the thread count. Default Hikari = 10 connections, so only 10 threads
// would really talk to the DB at once, and we would hide the race.
// size 60 because 50 threads + 10 for the main thread and the test framework.


//\- Optimistic: Use it when conflicts are rare and you are updating multiple fields in an entity (for example, editing restaurant details or a user profile from two different screens).
// `@Version` protects you against the "last write wins" problem without adding much complexity.
//
//\- Pessimistic: Use it when you have complex logic between reading and writing that cannot be converted into a single UPDATE statement
// (for example, reading from two tables, performing calculations, and then writing the result), and conflicts are frequent.
// You accept the cost of making transactions wait in a queue.
//
// \- Atomic: Use it when the condition involves a simple counter or state (inventory, balance, order status).
// It is the most suitable approach for most cases where you need to ensure that a value does not go below zero or change a state only if it is still in a specific condition.
@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.hikari.maximum-pool-size=60")
class OversellRaceTest extends AbstractIntegrationTest {


    static final int ITEM_ID = 1;
    static final int INITIAL_STOCK = 5;
    static final int THREADS = 50;

    @Autowired
    LabInventoryService service;

    @Autowired LabInventoryService labService; // add next to the existing 'service' if you prefer one name

    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void resetLab() {
        jdbc.update("DELETE FROM inventory_lab");
        jdbc.update("INSERT INTO inventory_lab (id, stock, version) VALUES (?, ?, 0)", ITEM_ID, INITIAL_STOCK);
    }

    private int currentStock() {
        return jdbc.queryForObject("SELECT stock FROM inventory_lab WHERE id = ?", Integer.class, ITEM_ID);
    }

    @Test
    void naive_oversells_the_last_meals() throws Exception {
        ConcurrencyTestUtil.Result r = ConcurrencyTestUtil.runConcurrently(THREADS, () -> service.buyNaive(ITEM_ID, 1));

        System.out.printf("NAIVE -> successes=%d rejected=%d errors=%d stock=%d (%d ms)%n",
                r.successes(), r.rejected(), r.errors(), currentStock(), r.millis());

        // We only had 5 meals but MORE than 5 purchases were accepted: this is the bug, proven.
        assertThat(r.successes()).isGreaterThan(INITIAL_STOCK);
        assertThat(currentStock()).isNegative();
    }


    @Test
    void optimistic_never_oversells_but_pays_in_retries() throws Exception {
        service.resetCounters();
        ConcurrencyTestUtil.Result r = ConcurrencyTestUtil.runConcurrently(THREADS, () -> service.buyOptimistic(ITEM_ID, 1));

        System.out.printf("OPTIMISTIC -> successes=%d rejected=%d errors=%d stock=%d retries=%d (%d ms)%n",
                r.successes(), r.rejected(), r.errors(), currentStock(), service.optimisticRetries(), r.millis());

        assertThat(r.successes()).isEqualTo(INITIAL_STOCK);
        assertThat(r.errors()).isZero();
        assertThat(currentStock()).isZero();
    }

    @Test
    void pessimistic_never_oversells() throws Exception {
        ConcurrencyTestUtil.Result r = ConcurrencyTestUtil.runConcurrently(THREADS, () -> service.buyPessimistic(ITEM_ID, 1));

        System.out.printf("PESSIMISTIC -> successes=%d rejected=%d errors=%d stock=%d (%d ms)%n",
                r.successes(), r.rejected(), r.errors(), currentStock(), r.millis());

        assertThat(r.successes()).isEqualTo(INITIAL_STOCK);
        assertThat(r.errors()).isZero();
        assertThat(currentStock()).isZero();
    }


    @Test
    void atomic_update_never_oversells() throws Exception {
        ConcurrencyTestUtil.Result r = ConcurrencyTestUtil.runConcurrently(THREADS, () -> service.buyAtomic(ITEM_ID, 1));

        System.out.printf("ATOMIC -> successes=%d rejected=%d errors=%d stock=%d (%d ms)%n",
                r.successes(), r.rejected(), r.errors(), currentStock(), r.millis());

        assertThat(r.successes()).isEqualTo(INITIAL_STOCK);
        assertThat(r.rejected()).isEqualTo(THREADS - INITIAL_STOCK);
        assertThat(currentStock()).isZero();
    }
}