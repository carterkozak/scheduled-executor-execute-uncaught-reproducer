## Reproducer for incorrect ScheduledThreadPoolExecutor.execute uncaught exception handling

Given a ThreadPoolExecutor we assume that uncaught exceptions are handled by `Thread.getUncaughtExceptionHandler()`
when a future is not returned. There is a common misconception that ScheduledThreadPoolExecutor schedule methods
should use the uncaught exception handler, however they return futures, so it's not necessary.

The `ScheduledThreadPoolExecutor.execute(Runnable)` method currently neither returns a future nor calls the
`UncaughtExceptionHandler` because internally it delegates to
`ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)` with a delay of zero, and ignores the
return value.

Reproducer main class:
[net.ckozak.ScheduledThreadPoolExecutorUncaughtException](src/main/java/net/ckozak/repro/ScheduledThreadPoolExecutorUncaughtException.java)