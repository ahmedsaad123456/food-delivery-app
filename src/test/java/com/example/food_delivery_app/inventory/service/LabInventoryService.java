package com.example.food_delivery_app.inventory.service;


import com.example.food_delivery_app.inventory.entity.LabItem;
import com.example.food_delivery_app.inventory.entity.LabItemVersioned;
import com.example.food_delivery_app.inventory.repository.LabItemRepository;
import com.example.food_delivery_app.inventory.repository.LabItemVersionedRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

@Service
public class LabInventoryService {
    private static final int MAX_ATTEMPTS = 10;

    private final LabItemRepository labItemRepository;

    private final LabItemVersionedRepository labItemVersionedRepository;


    private final TransactionTemplate tx;

    public LabInventoryService(LabItemRepository plain, LabItemVersionedRepository versioned ,TransactionTemplate tx) {
        this.labItemRepository = plain;
        this.labItemVersionedRepository=versioned;
        this.tx = tx;
    }

    // How many times optimistic locking had to retry. AtomicInteger because many threads update it.
    private final AtomicInteger optimisticRetries = new AtomicInteger();

    public int optimisticRetries() { return optimisticRetries.get(); }
    public void resetCounters() { optimisticRetries.set(0); }

    // NAIVE: check, then act. This is the bug.
    public boolean buyNaive(int id, int qty) {
        return Boolean.TRUE.equals(tx.execute(status -> {
            LabItem item = labItemRepository.findById(id).orElseThrow();
            if (item.getStock() < qty) return false;   // 1) CHECK
            sleepQuietly();                           // simulates real work (pricing, validation...). Widens the race window
            labItemRepository.deductUnguarded(id, qty);            // 2) ACT: nobody re-checks
            return true;
        }));
    }

    // ---- 02: optimistic ----
    public boolean buyOptimistic(int id, int qty) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                // The retry loop is OUTSIDE the transaction: every attempt gets a brand-new transaction
                // and a fresh read. Retrying inside the same transaction would re-use stale data.
                return Boolean.TRUE.equals(tx.execute(status -> {
                    LabItemVersioned item = labItemVersionedRepository.findById(id).orElseThrow();
                    if (item.getStock() < qty) return false;
                    item.setStock(item.getStock() - qty);
                    return true; // the UPDATE ... WHERE version = ? runs at commit (flush)
                }));
            } catch (OptimisticLockingFailureException e) {
                optimisticRetries.incrementAndGet();
                sleepQuietly(ThreadLocalRandom.current().nextLong(1, 10)); // jitter: avoid retrying in lock-step
            }
        }
        throw new IllegalStateException("gave up after " + MAX_ATTEMPTS + " optimistic attempts");
    }

    // ---- 03: pessimistic ----
    public boolean buyPessimistic(int id, int qty) {
        return Boolean.TRUE.equals(tx.execute(status -> {
            LabItem item = labItemRepository.findByIdForUpdate(id).orElseThrow(); // SELECT ... FOR UPDATE: waits if locked
            if (item.getStock() < qty) return false;
            item.setStock(item.getStock() - qty);  // dirty checking writes it at commit, still holding the lock
            return true;
        }));                                       // commit releases the lock, next thread wakes up
    }

    // ---- 04: atomic conditional update ----
    public boolean buyAtomic(int id, int qty) {
        // UPDATE ... WHERE stock >= ? is atomic: either it decrements and returns 1 row, or it does nothing and returns 0 rows.
        // No SELECT, no lock, no dirty checking, no retry loop. The database does the work.
        // At the same time, it is the simplest and most efficient solution. It is also the one that scales best under high contention.
        Integer rows = tx.execute(status -> labItemRepository.deductAtomic(id, qty));
        return rows != null && rows == 1; // 1 row = deducted, 0 rows = not enough stock
    }

    private static void sleepQuietly(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static void sleepQuietly() {
        try { sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}