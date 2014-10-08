package com.rocksoft.grinder

import com.rocksoft.grinder.event.GrinderQEvent
import com.rocksoft.grinder.event.GrinderQEventListener
import com.rocksoft.grinder.event.GrinderQEventType
import spock.lang.Specification

import java.util.concurrent.ScheduledExecutorService

class PoolMonitorSpec extends Specification {

  def "Logs time when a queued message is received"() {
    setup:
    ScheduledExecutorService mockPool = Mock()
    PoolMonitor monitor = new PoolMonitor(mockPool)

    when:
    monitor.logReceived()

    then:
    monitor.lastQueueEntryReceived
  }

  def "Shuts down monitor pool after timeout has elapsed"() {
    setup:
    ScheduledExecutorService mockPool = Mock()
    PoolMonitor monitor = new PoolMonitor(mockPool)
    GrinderQEventListener listener = Mock()
    monitor.eventListeners = [listener]
    monitor.setTimeout(666L)
    monitor.lastQueueEntryReceived = System.currentTimeMillis() - 1000L

    when:
    monitor.logEmpty()

    then:
    1 * mockPool.shutdownNow()
    1 * listener.queueEventReceived({ it.eventType == GrinderQEventType.QUEUE_STOPPED } as GrinderQEvent);
  }

  def "Does not shut down pool if timeout has not elapsed"() {
    setup:
    ScheduledExecutorService mockPool = Mock()
    PoolMonitor monitor = new PoolMonitor(mockPool)
    monitor.setTimeout(666L)
    monitor.lastQueueEntryReceived = System.currentTimeMillis() - 600L

    when:
    monitor.logEmpty()

    then:
    0 * mockPool._
  }

  def "Adds event listener"() {
   setup:
   PoolMonitor monitor = new PoolMonitor(null)
   GrinderQEventListener mockListener = Mock(GrinderQEventListener)

    when:
    monitor.addEventListener(mockListener)

    then:
    monitor.eventListeners.size() == 1
    monitor.eventListeners.first() == mockListener
  }
}
