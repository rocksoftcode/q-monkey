package com.rocksoft.grinder;

public interface GrinderConsumer<T> {

  void consume(T item);

}