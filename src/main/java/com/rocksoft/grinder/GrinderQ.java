package com.rocksoft.grinder;

import com.rocksoft.grinder.event.GrinderQEventListener;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GrinderQ<T> {
  private ScheduledExecutorService executorService;
  private Queue<T> delegate;
  int poolSize;
  boolean isRunning;
  PoolMonitor poolMonitor;

  private static final Pulse DEFAULT_PULSE = Pulse.EXTRA_FAST;
  private static final long DEFAULT_TIMEOUT = 5 * 60 * 1000;
  private static final boolean DEFAULT_SHOULD_SHUTDOWN_ON_TIMEOUT = true;

  /**
   * Constructs a new Grinder Queue
   *
   * @param numberThreads The maximum number of threads to apply to this queue
   */
  public GrinderQ(int numberThreads) {
    executorService = Executors.newScheduledThreadPool(numberThreads);
    poolMonitor = new PoolMonitor(executorService);
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
   * Starts listening to the queue, operating on new entries. If the timeout is reached since the last queue activity, and
   * the queue is set to shutdownOnTimeout, it will broadcast a GrinderQEventType.QUEUE_TIMEOUT even, shutdown, and
   * broadcast a GrinderQEventType.QUEUE_STOPPED event.
   *
   * In scenarios where the queue should be kept running, shouldShutdownOnTimeout should be set to false, and a
   * GrinderQEventType.QUEUE_TIMEOUT event is broadcast.
   *
   * @param consumer                An implementation of GrinderConsumer that will operate on a queue element
   * @param pulse                   The frequency with which the queue is checked
   * @param timeout                 The amount of time, in milliseconds, the queue will stay alive without activity
   * @param shouldShutdownOnTimeout Whether the queue should shutdown when the timeout is reached.
   */
  public void start(GrinderConsumer<T> consumer, Pulse pulse, long timeout, boolean shouldShutdownOnTimeout) {
    if (isRunning) {
      throw new IllegalStateException("Queue is already running");
    } else {
      poolMonitor.setTimeout(timeout);
      poolMonitor.setShouldShutdownOnTimeout(shouldShutdownOnTimeout);
      for (int i = 0; i < poolSize; i++) {
        executorService.scheduleWithFixedDelay(new PoolPoller<T>(delegate, poolMonitor, consumer), 0L, pulse.value(), TimeUnit.MILLISECONDS);
      }
      isRunning = true;
    }
  }

  /**
   * Starts listening to the queue, operating on new entries with the default pulse and timeout/shutdown settings
   *
   * @param consumer An implementation of GrinderConsumer that will operate on a queue element
   */
  public void start(GrinderConsumer<T> consumer) {
    start(consumer, DEFAULT_PULSE, DEFAULT_TIMEOUT, DEFAULT_SHOULD_SHUTDOWN_ON_TIMEOUT);
  }

  /**
   * Adds a listener to the queue in order to respond to state change events
   *
   * @param listener any object that implements GrinderQEventListener
   */
  public void addQueueEventListener(GrinderQEventListener listener) {
    poolMonitor.addEventListener(listener);
  }
}