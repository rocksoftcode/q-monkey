package com.rocksoft.grinder

import spock.lang.Specification

class PoolPollerSpec extends Specification {

  def "Poller calls consumer"() {
    setup:
    GrinderConsumer<String> mockConsumer = Mock()
    PoolMonitor mockMonitor = Mock()
    PoolPoller poller = new PoolPoller(new ArrayDeque<String>(), mockMonitor, mockConsumer)
    poller.delegate.offer("foobar")

    when:
    poller.run()

    then:
    1 * poller.consumer.consume("foobar")
    1 * mockMonitor.logReceived()
  }


  def "Poller logs empty status of queue"() {
    setup:
    GrinderConsumer<String> mockConsumer = Mock()
    PoolMonitor mockMonitor = Mock()
    PoolPoller poller = new PoolPoller(new ArrayDeque<String>(), mockMonitor, mockConsumer)

    when:
    poller.run()

    then:
    0 * poller.consumer._
    1 * mockMonitor.logEmpty()
  }

  def "Poller swallows exception"() {
    setup:
    GrinderConsumer<String> mockConsumer = Mock()
    PoolMonitor mockMonitor = Mock()
    PoolPoller poller = new PoolPoller(new ArrayDeque<String>(), mockMonitor, mockConsumer)
    poller.delegate.offer("foobar")

    when:
    poller.run()

    then:
    1 * poller.consumer.consume("foobar") >> { throw new Exception("!") }
    notThrown(Exception)
  }
}