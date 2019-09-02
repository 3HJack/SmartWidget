package com.hhh.smartwidget.immersive;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.view.Window;

import androidx.annotation.NonNull;

public class MIUIImmersiveUtils {
  private MIUIImmersiveUtils() {}

  public static boolean setMIUILightStatusBar(@NonNull Activity activity, boolean dark) {
    try {
      Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
      Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
      int darkModeFlag = field.getInt(layoutParams);
      Class<? extends Window> clazz = activity.getWindow().getClass();
      Method setExtraFlags = clazz.getMethod("setExtraFlags", int.class, int.class);
      setExtraFlags.invoke(activity.getWindow(), dark ? darkModeFlag : 0, darkModeFlag);
      return true;
    } catch (Exception e) {}
    return false;
  }
}
