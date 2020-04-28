package com.hhh.smartwidget.immersive;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.hhh.smartwidget.WidgetUtils;

public final class ImmersiveUtils {

  private ImmersiveUtils() {}

  public static void enterImmersive(@NonNull Activity activity) {
    if (!(activity instanceof Immersive)) {
      return;
    }
    Immersive immersive = (Immersive) activity;
    if (immersive.customImmersive()) {
      return;
    }
    if (Build.VERSION.SDK_INT < immersive.minImmersiveVersionCode()) {
      return;
    }
    TypedArray array =
        activity.getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowFullscreen});
    if (!array.getBoolean(0, false)) {
      enterImmersive(activity, immersive.statusBarColor(), immersive.darkImmersive());
      activity.findViewById(android.R.id.content).setPadding(0,
          WidgetUtils.getStatusBarHeight(activity), 0, 0);
    }
    array.recycle();
  }

  public static void enterImmersive(@NonNull Activity activity, int color, boolean dark) {
    Window window = activity.getWindow();
    int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (dark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        option |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (WidgetUtils.isMIUI()) {
          MIUIImmersiveUtils.setMIUILightStatusBar(activity, true);
        } else if (WidgetUtils.isFlyme()) {
          FlymeImmersiveUtils.setStatusBarDarkIcon(activity, true);
        }
      }
      window.getDecorView().setSystemUiVisibility(option);
      window.setStatusBarColor(color);
      window.setNavigationBarColor(window.getNavigationBarColor()); // 适配某些机型
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      window.getDecorView().setSystemUiVisibility(option);
    }
  }

  public static boolean isImmersiveMode(@NonNull Activity activity) {
    int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    return (activity.getWindow().getDecorView().getSystemUiVisibility() & option) == option
        && !WidgetUtils.isFullScreen(activity.getWindow());
  }
}
