package com.rocksoft.grinder.event;

public class GrinderQEvent  {
  private GrinderQEventType eventType;

  public GrinderQEvent(GrinderQEventType eventType) {
    this.eventType = eventType;
  }

  public GrinderQEventType getEventType() {
    return eventType;
  }
}