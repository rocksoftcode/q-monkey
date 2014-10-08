package com.rocksoft.grinder;

import java.util.concurrent.ScheduledExecutorService;

public class PoolMonitor {
  long lastQueueEntryReceived;
  long timeout;
  ScheduledExecutorService pool;

  public PoolMonitor(ScheduledExecutorService pool, long timeout) {
    this.pool = pool;
    this.timeout = timeout;
  }

  void logReceived() {
    lastQueueEntryReceived = System.currentTimeMillis();
  }

  void logEmpty() {
    if (System.currentTimeMillis() - lastQueueEntryReceived > timeout) {
      pool.shutdownNow();
    }
  }
}
