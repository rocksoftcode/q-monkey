package com.rocksoft.grinder;

public enum Pulse {
  EXTRA_SLOW(30000L), SLOW(5000L), NORMAL(1000L), FAST(500L), EXTRA_FAST(1L);

  private long value;

  Pulse(long value) {
    this.value = value;
  }

  public long value() {
    return value;
  }

}
