package com.rocksoft.grinder.event;

import java.util.EventListener;

public interface GrinderQEventListener extends EventListener {

  void queueEventReceived(GrinderQEvent event);

}