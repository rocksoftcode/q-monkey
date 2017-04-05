package com.rocksoft.qmonkey;

import java.util.Queue;

class PoolPoller<T> implements Runnable {

  Queue<T> delegate;
  QMonkeyConsumer<T> consumer;
  PoolMonitor monitor;

  PoolPoller(Queue<T> delegate, PoolMonitor monitor, QMonkeyConsumer<T> consumer) {
    this.delegate = delegate;
    this.consumer = consumer;
    this.monitor = monitor;
  }

  @Override
  public void run() {
    try {
      T head = delegate.poll();
      if (head != null) {
        monitor.logReceived();
        consumer.consume(head);
      } else {
        monitor.logEmpty();
      }
    } catch (Exception e) {
      System.out.println("Caught exception: " + e.getMessage());
    }
  }
}
