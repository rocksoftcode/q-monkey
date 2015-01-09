package com.rocksoft.grinder

import com.rocksoft.grinder.event.GrinderQEvent
import com.rocksoft.grinder.event.GrinderQEventListener
import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GrinderQSpec extends Specification {

  def "Initializes correctly"() {
    setup:
    GrinderQ<String> q = new GrinderQ<>(6)

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
    GrinderQ<String> q = new GrinderQ<>(6)

    when:
    q.offer("foobar")

    then:
    q.delegate.size() == 1
    q.delegate.poll() == "foobar"
  }

  def "Creates threads with consumer tasks"() {
    setup:
    GrinderQ<String> q = new GrinderQ<>(2)
    q.executorService = Mock(ScheduledExecutorService)

    when:
    q.start(new TestConsumer(), Pulse.EXTRA_FAST, 1L, true)

    then:
    2 * q.executorService.scheduleWithFixedDelay({ it.monitor.timeout == 1L && it.delegate == q.delegate && it.consumer instanceof TestConsumer } as PoolPoller<String>, 0L, Pulse.EXTRA_FAST.value(), TimeUnit.MILLISECONDS)
  }

  def "Creates threads with consumer tasks using default values for pulse and timeout"() {
    setup:
    GrinderQ<String> q = new GrinderQ<>(2)
    q.executorService = Mock(ScheduledExecutorService)

    when:
    q.start(new TestConsumer())

    then:
    2 * q.executorService.scheduleWithFixedDelay({ it.monitor.timeout == 300000 && it.delegate == q.delegate && it.consumer instanceof TestConsumer } as PoolPoller<String>, 0L, Pulse.EXTRA_FAST.value(), TimeUnit.MILLISECONDS)
  }

  def "Does not start threads if they are already running"() {
    setup:
    GrinderQ<String> q = new GrinderQ<>(2)
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
    GrinderQ<String> q = new GrinderQ<>(2)
    q.poolMonitor = Mock(PoolMonitor)
    TestListener listener = new TestListener()

    when:
    q.addQueueEventListener(listener)

    then:
    1 * q.poolMonitor.addEventListener(listener)
  }

  static class TestListener implements GrinderQEventListener {
    @Override
    void queueEventReceived(GrinderQEvent event) {
    }
  }

  static class TestConsumer implements GrinderConsumer<String> {

    @Override
    void consume(String item) {
    }
  }
}
