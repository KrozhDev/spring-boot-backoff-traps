package dev.backofftraps.traps.executor;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

/**
 * Reports the thread an {@code @Async} call runs on: shows which executor Spring picked.
 */
public class AsyncWorker {

    @Async
    public CompletableFuture<String> threadName() {
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }
}
