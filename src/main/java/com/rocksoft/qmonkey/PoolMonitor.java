package com.rocksoft.qmonkey;

import com.rocksoft.qmonkey.event.QMonkeyEvent;
import com.rocksoft.qmonkey.event.QMonkeyEventListener;
import com.rocksoft.qmonkey.event.QMonkeyEventType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PoolMonitor {
  long lastQueueEntryReceived = System.currentTimeMillis();
  long timeout;
  ScheduledExecutorService pool;
  Set<QMonkeyEventListener> eventListeners = new HashSet<>();

  public PoolMonitor(ScheduledExecutorService pool) {
    this.pool = pool;
  }

  void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  void logReceived() {
    lastQueueEntryReceived = System.currentTimeMillis();
  }

  void logEmpty() {
    if (System.currentTimeMillis() - lastQueueEntryReceived >= timeout) {
      shutDown();
    }
  }

  private void shutDown() {
    pool.shutdown();
    try {
      pool.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    broadcastEvent(QMonkeyEventType.QUEUE_STOPPED);
  }

  public void addEventListener(QMonkeyEventListener listener) {
    eventListeners.add(listener);
  }

  void broadcastEvent(QMonkeyEventType eventType) {
    for (QMonkeyEventListener listener : eventListeners) {
      listener.queueEventReceived(new QMonkeyEvent(eventType));
    }
  }
}
