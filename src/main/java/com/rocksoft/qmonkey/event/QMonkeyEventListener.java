package com.rocksoft.qmonkey.event;

import java.util.EventListener;

public interface QMonkeyEventListener extends EventListener {

  void queueEventReceived(QMonkeyEvent event);

}