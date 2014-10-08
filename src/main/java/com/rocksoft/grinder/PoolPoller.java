package com.rocksoft.grinder;

import java.util.Queue;

class PoolPoller<T> implements Runnable {

  Queue<T> delegate;
  GrinderConsumer<T> consumer;
  PoolMonitor monitor;

  PoolPoller(Queue<T> delegate, PoolMonitor monitor, GrinderConsumer<T> consumer) {
    this.delegate = delegate;
    this.consumer = consumer;
    this.monitor = monitor;
  }

  @Override
  public void run() {
    try {
      T head = delegate.poll();
      if (head != null) {
        consumer.consume(head);
        monitor.logReceived();
      } else {
        monitor.logEmpty();
      }
    } catch (Exception e) {
      System.out.println("Caught exception: " + e.getMessage());
    }
  }
}
