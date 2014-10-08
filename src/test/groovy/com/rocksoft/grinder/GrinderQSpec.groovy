package com.rocksoft.grinder

import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GrinderQSpec extends Specification {

  def "Initializes correctly"() {
    when:
    GrinderQ<String> q = new GrinderQ<>(6)

    then:
    q.executorService.corePoolSize == 6
    q.poolSize == 6
    ArrayBlockingQueue<String> delegate = q.delegate
    delegate.remainingCapacity() == 60000
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
    q.start(new TestConsumer(), Pulse.EXTRA_FAST, 1L)

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


  static class TestConsumer implements GrinderConsumer<String> {

    @Override
    void consume(String item) {
    }
  }
}
