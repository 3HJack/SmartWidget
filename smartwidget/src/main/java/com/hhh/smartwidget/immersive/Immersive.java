package com.hhh.smartwidget.immersive;

import android.graphics.Color;
import android.os.Build;

public interface Immersive {

  default boolean customImmersive() {
    return false;
  }

  default boolean darkImmersive() {
    return true;
  }

  default int statusBarColor() {
    return Color.WHITE;
  }

  default int minImmersiveVersionCode() {
    return Build.VERSION_CODES.M;
  }
}
