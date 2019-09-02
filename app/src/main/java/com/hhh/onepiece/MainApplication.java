package com.hhh.onepiece;

import android.app.Application;

import com.hhh.smartwidget.SmartWidget;

public class MainApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    SmartWidget.init(new SmartWidget.Config(this));
  }
}
