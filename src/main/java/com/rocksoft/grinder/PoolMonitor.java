package com.rocksoft.grinder;

import com.rocksoft.grinder.event.GrinderQEvent;
import com.rocksoft.grinder.event.GrinderQEventListener;
import com.rocksoft.grinder.event.GrinderQEventType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class PoolMonitor {
  long lastQueueEntryReceived = System.currentTimeMillis();
  long timeout;
  ScheduledExecutorService pool;
  Set<GrinderQEventListener> eventListeners = new HashSet<>();

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
    pool.shutdownNow();
    for (GrinderQEventListener listener : eventListeners) {
      listener.queueEventReceived(new GrinderQEvent(GrinderQEventType.QUEUE_STOPPED));
    }
  }

  public void addEventListener(GrinderQEventListener listener) {
    eventListeners.add(listener);
  }
}
