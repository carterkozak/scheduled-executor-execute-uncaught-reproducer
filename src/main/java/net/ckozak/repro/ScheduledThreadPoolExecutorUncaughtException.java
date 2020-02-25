package net.ckozak.repro;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ScheduledThreadPoolExecutorUncaughtException {

    public static void main(String[] args) throws InterruptedException {
        // Track uncaught exceptions for verification
        List<Throwable> uncaught = new CopyOnWriteArrayList<>();
        ExecutorService executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName("scheduled-" + counter.getAndIncrement());
                thread.setUncaughtExceptionHandler((thread1, throwable) -> {
                    uncaught.add(throwable);
                    throwable.printStackTrace();
                });
                return thread;
            }
        });
        try {
            // Failure should be reported in some way.
            // Using a ThreadPoolExecutor it will be reported to the UncaughtExceptionHandler
            // (we can replace 'new ScheduledThreadPoolExecutor(' with 'Executors.newFixedThreadPool('
            // to observe the expected behavior.
            executor.execute(() -> { throw new RuntimeException(); });
        } finally {
            executor.shutdown();
        }
        if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Executor failed to stop within 1 second");
        }
        // Task completion may race termination
        Thread.sleep(100);
        if (uncaught.size() != 1) {
            System.err.println("Expected an uncaught exception but received: " + uncaught);
            System.exit(1);
        } else {
            System.out.println("Success");
        }
    }
}
