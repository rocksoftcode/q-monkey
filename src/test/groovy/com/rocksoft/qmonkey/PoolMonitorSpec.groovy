package com.rocksoft.qmonkey

import com.rocksoft.qmonkey.event.QMonkeyEvent
import com.rocksoft.qmonkey.event.QMonkeyEventListener
import com.rocksoft.qmonkey.event.QMonkeyEventType
import spock.lang.Specification

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

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

  def "Shuts down monitor pool after timeout has elapsed, broadcasts QUEUE_STOPPED"() {
    setup:
    ScheduledExecutorService mockPool = Mock()
    PoolMonitor monitor = new PoolMonitor(mockPool)
    QMonkeyEventListener listener = Mock()
    monitor.eventListeners = [listener]
    monitor.setTimeout(666L)
    monitor.lastQueueEntryReceived = System.currentTimeMillis() - 1000L

    when:
    monitor.logEmpty()

    then:
    1 * mockPool.shutdown()
    1 * mockPool.awaitTermination(10, TimeUnit.SECONDS)
    1 * listener.queueEventReceived({ it.eventType == QMonkeyEventType.QUEUE_STOPPED } as QMonkeyEvent)
    0 * listener.queueEventReceived(_)
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
   QMonkeyEventListener mockListener = Mock(QMonkeyEventListener)

    when:
    monitor.addEventListener(mockListener)

    then:
    monitor.eventListeners.size() == 1
    monitor.eventListeners.first() == mockListener
  }
}
