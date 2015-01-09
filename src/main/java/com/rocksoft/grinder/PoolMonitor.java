package com.rocksoft.grinder;

import com.rocksoft.grinder.event.GrinderQEvent;
import com.rocksoft.grinder.event.GrinderQEventListener;
import com.rocksoft.grinder.event.GrinderQEventType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PoolMonitor {
  long lastQueueEntryReceived = System.currentTimeMillis();
  long timeout;
  ScheduledExecutorService pool;
  Set<GrinderQEventListener> eventListeners = new HashSet<>();
  boolean shouldShutdownOnTimeout = true;

  public PoolMonitor(ScheduledExecutorService pool) {
    this.pool = pool;
  }

  void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  void setShouldShutdownOnTimeout(boolean shouldShutdownOnTimeout) { this.shouldShutdownOnTimeout = shouldShutdownOnTimeout; }

  void logReceived() {
    lastQueueEntryReceived = System.currentTimeMillis();
  }

  void logEmpty() {
    if (System.currentTimeMillis() - lastQueueEntryReceived >= timeout) {
      broadcastEvent(GrinderQEventType.QUEUE_TIMEOUT);
      if (shouldShutdownOnTimeout) {
        shutDown();
      }
    }
  }

  private void shutDown() {
    pool.shutdown();
    broadcastEvent(GrinderQEventType.QUEUE_STOPPED);
    try {
      pool.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void addEventListener(GrinderQEventListener listener) {
    eventListeners.add(listener);
  }

  private void broadcastEvent(GrinderQEventType eventType) {
    for (GrinderQEventListener listener : eventListeners) {
      listener.queueEventReceived(new GrinderQEvent(eventType));
    }
  }
}
