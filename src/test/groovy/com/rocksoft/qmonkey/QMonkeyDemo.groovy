package com.rocksoft.qmonkey

class QMonkeyDemo {

  static void main(String[] args) {
    QMonkeyMonkey<String> q = new QMonkeyMonkey<>(21)
    (1..1000).each { i ->
      q.offer('foobar' + i)
    }
    q.start(new DemoConsumer(), Pulse.EXTRA_FAST, 100L)
    Thread.sleep(2000L)

    assert DemoConsumer.count == 1000
  }

  static class DemoConsumer implements QMonkeyConsumer<String> {
    static int count
    void consume(String item) {
      count++
    }
  }
}
