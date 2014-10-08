package com.rocksoft.grinder;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GrinderQ<T> {
  private ScheduledExecutorService executorService;
  private Queue<T> delegate;
  int poolSize;

  private static final Pulse DEFAULT_PULSE = Pulse.EXTRA_FAST;
  private static final long DEFAULT_TIMEOUT = 5 * 60 * 1000;

  /**
   * Constructs a new Grinder Queue
   *
   * @param numberThreads The maximum number of threads to apply to this queue
   */
  public GrinderQ(int numberThreads) {
    executorService = Executors.newScheduledThreadPool(numberThreads);
    delegate = new ArrayBlockingQueue<T>(numberThreads * 10000);
    poolSize = numberThreads;
  }

  /**
   * Wraps offer(T t) for the delegate
   *
   * @param t the value offered to the queue
   * @return true if the value went onto the queue, false if not
   */
  public boolean offer(T t) {
    return delegate.offer(t);
  }

  /**
   * Starts listening to the queue, operating on new entries
   *
   * @param consumer An implementation of GrinderConsumer that will operate on a queue element
   * @param pulse   The frequency with which the queue is checked
   * @param timeout The amount of time, in milliseconds, the queue will stay alive without activity
   */
  public void start(GrinderConsumer<T> consumer, Pulse pulse, long timeout) {
    PoolMonitor poolMonitor = new PoolMonitor(executorService, timeout);
    for (int i = 0; i < poolSize; i++) {
      executorService.scheduleWithFixedDelay(new PoolPoller<T>(delegate, poolMonitor, consumer), 0L, pulse.value(), TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Starts listening to the queue, operating on new entries with the default pulse and timeout settings
   *
   * @param consumer An implementation of GrinderConsumer that will operate on a queue element
   */
  public void start(GrinderConsumer<T> consumer) {
    start(consumer, DEFAULT_PULSE, DEFAULT_TIMEOUT);
  }
}