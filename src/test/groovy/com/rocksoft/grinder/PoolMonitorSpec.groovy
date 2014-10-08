package com.rocksoft.grinder

import spock.lang.Specification

import java.util.concurrent.ScheduledExecutorService

class PoolMonitorSpec extends Specification {

  def "Logs time when a queued message is received"() {
    setup:
    ScheduledExecutorService mockPool = Mock()
    PoolMonitor monitor = new PoolMonitor(mockPool, 10000L)

    when:
    monitor.logReceived()

    then:
    monitor.lastQueueEntryReceived
  }

  def "Shuts down monitor pool after timeout has elapsed"() {
    setup:
    ScheduledExecutorService mockPool = Mock()
    PoolMonitor monitor = new PoolMonitor(mockPool, 666L)
    monitor.lastQueueEntryReceived = System.currentTimeMillis() - 1000L

    when:
    monitor.logEmpty()

    then:
    1 * mockPool.shutdownNow()
  }

  def "Does not shut down pool if timeout has not elapsed"() {
    setup:
    ScheduledExecutorService mockPool = Mock()
    PoolMonitor monitor = new PoolMonitor(mockPool, 666L)
    monitor.lastQueueEntryReceived = System.currentTimeMillis() - 600L

    when:
    monitor.logEmpty()

    then:
    0 * mockPool._
  }
}
