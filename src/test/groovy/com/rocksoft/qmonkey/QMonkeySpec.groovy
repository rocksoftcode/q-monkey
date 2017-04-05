package com.rocksoft.qmonkey

import com.rocksoft.qmonkey.event.QMonkeyEvent
import com.rocksoft.qmonkey.event.QMonkeyEventListener
import com.rocksoft.qmonkey.event.QMonkeyEventType
import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class QMonkeySpec extends Specification {

  def "Initializes correctly"() {
    setup:
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(6)

    expect:
    q.executorService.corePoolSize == 6
    q.poolSize == 6
    ArrayBlockingQueue<String> delegate = q.delegate
    delegate.remainingCapacity() == 60000

    where:
    shouldShutdown << [false, true]
  }

  def "Offer delegates to wrapped queue"() {
    setup:
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(6)

    when:
    q.offer("foobar")

    then:
    q.delegate.size() == 1
    q.delegate.poll() == "foobar"
  }

  def "Creates threads with consumer tasks"() {
    setup:
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(2)
    q.executorService = Mock(ScheduledExecutorService)

    when:
    q.start(new TestConsumer(), 0L, Pulse.EXTRA_FAST, 1L)

    then:
    2 * q.executorService.scheduleWithFixedDelay({ it.monitor.timeout == 1L && it.delegate == q.delegate && it.consumer instanceof TestConsumer } as PoolPoller<String>, 0L, Pulse.EXTRA_FAST.value(), TimeUnit.MILLISECONDS)
  }

  def "Creates threads with consumer tasks using default values for pulse and timeout"() {
    setup:
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(2)
    q.executorService = Mock(ScheduledExecutorService)

    when:
    q.start(new TestConsumer())

    then:
    2 * q.executorService.scheduleWithFixedDelay({ it.monitor.timeout == 300000 && it.delegate == q.delegate && it.consumer instanceof TestConsumer } as PoolPoller<String>, 500L, Pulse.EXTRA_FAST.value(), TimeUnit.MILLISECONDS)
    q.isRunning()
  }

  def "Does not start threads if they are already running"() {
    setup:
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(2)
    q.executorService = Mock(ScheduledExecutorService)
    q.start(new TestConsumer())

    when:
    q.start(new TestConsumer())

    then:
    IllegalStateException e = thrown()
    e.message == "Queue is already running"
  }

  def "Adds listener to pool monitor"() {
    setup:
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(2)
    q.poolMonitor = Mock(PoolMonitor)
    TestListener listener = new TestListener()

    when:
    q.addQueueEventListener(listener)

    then:
    1 * q.poolMonitor.addEventListener(listener)
  }

  def "Handles queue stopped event"() {
    setup:
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(2)
    q.executorService = Mock(ScheduledExecutorService)
    q.start(new TestConsumer())

    when:
    q.poolMonitor.broadcastEvent(QMonkeyEventType.QUEUE_STOPPED)

    then:
    !q.isRunning()
  }

  static class TestListener implements QMonkeyEventListener {
    @Override
    void queueEventReceived(QMonkeyEvent event) {
    }
  }

  static class TestConsumer implements QMonkeyConsumer<String> {

    @Override
    void consume(String item) {
    }
  }
}
