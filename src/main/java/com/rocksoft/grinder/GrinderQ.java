package com.rocksoft.grinder;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GrinderQ<T> {
  private ScheduledExecutorService executorService;
  private Queue<T> delegate;
  Class<GrinderConsumer<T>> consumerType;
  int poolSize;

  /**
   * Constructs a new Grinder Queue
   *
   * @param numberThreads The maximum number of threads to apply to this queue
   * @param consumerType The class representing the consumer event you have created to handle queue items
   */
  public GrinderQ(int numberThreads, Class<GrinderConsumer<T>> consumerType) {
    executorService = Executors.newScheduledThreadPool(numberThreads);
    delegate = new ArrayBlockingQueue<T>(numberThreads * 10000);
    this.consumerType = consumerType;
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
   * @param pulse The frequency with which the queue is checked
   * @param timeout The amount of time, in milliseconds, the queue will stay alive without activity
   */
  public void start(Pulse pulse, long timeout) {
    PoolMonitor poolMonitor = new PoolMonitor(executorService, timeout);
    for (int i=0; i < poolSize; i++) {
      try {
        executorService.scheduleWithFixedDelay(new PoolPoller<T>(delegate, poolMonitor, consumerType.newInstance()), 0L, pulse.value(), TimeUnit.MILLISECONDS);
      } catch (ReflectiveOperationException e) {
        System.err.println("Could not schedule task. Cause: " + e.getMessage());
      }
    }
  }
}