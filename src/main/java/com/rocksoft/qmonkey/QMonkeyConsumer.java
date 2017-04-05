package com.rocksoft.qmonkey;

public interface QMonkeyConsumer<T> {

  void consume(T item);

}