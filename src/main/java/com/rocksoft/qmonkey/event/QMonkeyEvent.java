package com.rocksoft.qmonkey.event;

public class QMonkeyEvent {
  private QMonkeyEventType eventType;

  public QMonkeyEvent(QMonkeyEventType eventType) {
    this.eventType = eventType;
  }

  public QMonkeyEventType getEventType() {
    return eventType;
  }
}