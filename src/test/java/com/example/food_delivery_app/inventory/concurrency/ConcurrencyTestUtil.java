package com.example.food_delivery_app.inventory.concurrency;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * THE BIG IDEA:
 * This utility runs a "race". It prepares N threads, lines them all up at the start line,
 * then fires a starting gun, so they all run at the same moment.
 * Goal: simulate many users doing the same operation at once (e.g. 100 people trying to
 * buy the last items in stock) and check whether our code survives or has a concurrency bug.
 */


public final class ConcurrencyTestUtil {


    private ConcurrencyTestUtil() {}

    // The "result sheet" returned at the end. A record is a short way to declare a class
    // that only holds data.
    // successes = task returned true  (operation accepted, e.g. the purchase went through)
    // rejected  = task returned false (operation refused, e.g. out of stock. A normal business answer, not a problem)
    // errors    = task threw an exception (a real failure, e.g. gave up after retries)
    // millis    = how long the race took, in milliseconds
    public record Result(int successes, int rejected, int errors, long millis) {}

    // The main method.
    // threads: how many threads to run (how many "users" act at the same time)
    // task   : the job each thread runs. Callable<Boolean> = something that runs and returns true or false
    public static Result runConcurrently(int threads, Callable<Boolean> task) throws Exception {

        // A pool of workers (thread pool) with a fixed size = threads
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        // CountDownLatch = a countdown counter. Think of it as a locked gate
        // that opens when the counter reaches zero.

        // "I am standing on the start line": each thread decrements it once. Zero = everyone is ready.
        CountDownLatch ready = new CountDownLatch(threads);

        // The starting gun: counter is just 1. Once it is decremented, every waiting thread runs at once.
        CountDownLatch go = new CountDownLatch(1);

        // "I finished": each thread decrements it when done. Zero = everyone has finished.
        CountDownLatch done = new CountDownLatch(threads);

        // Three thread-safe counters for the results.
        // Why not a plain int? If two threads read 5 at the same time and each writes 6,
        // we lose one increment. AtomicInteger guarantees every increment is counted.
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();

        // Loop once per thread: each iteration tells the pool "take this job and run it"
        for (int i = 0; i < threads; i++) {
            // () -> { ... } is a lambda = a small nameless function; it is the work each thread does
            pool.submit(() -> {

                ready.countDown(); // The thread says "I'm on the start line" (decrements ready by 1)

                try {
                    go.await(); // Wait here! Do not continue until the starting gun fires

                    // The gun fired: run the real task and look at its result
                    if (task.call()) successes.incrementAndGet(); // returned true  -> success
                    else rejected.incrementAndGet();              // returned false -> normal rejection

                } catch (Exception e) {
                    // The task threw an exception -> a real failure, count it
                    errors.incrementAndGet();

                } finally {
                    // finally = runs no matter what (success or failure).
                    // If we skipped this on failure, done would never reach zero
                    // and the test would hang forever.
                    done.countDown(); // "I finished"
                }
            });
        }

        // The main thread waits until ALL worker threads are on the start line
        ready.await();

        // Record the start time (in nanoseconds) so we can compute the duration later
        long start = System.nanoTime();

        // FIRE THE GUN! Decrements go from 1 to 0, the gate opens, and all threads run together
        go.countDown();

        // Wait for all threads to finish, but at most 30 seconds.
        // Returns true if everyone finished in time, false if time ran out and someone is still running.
        // The timeout matters: if there is a deadlock, the test fails instead of hanging forever.
        boolean finished = done.await(30, TimeUnit.SECONDS);

        // Shut down the pool and stop anything still running
        pool.shutdownNow();

        // Time ran out and not everyone finished -> probably a deadlock
        // (thread A waiting for thread B while B waits for A)
        if (!finished) throw new IllegalStateException("threads did not finish in 30 seconds (deadlock?)");

        // Build the result sheet:
        // .get() reads the current value of each counter
        // (now - start) / 1,000,000 converts nanoseconds to milliseconds
        // The _ in 1_000_000 is only there to make the number easier to read
        return new Result(successes.get(), rejected.get(), errors.get(),
                (System.nanoTime() - start) / 1_000_000);
    }
}
