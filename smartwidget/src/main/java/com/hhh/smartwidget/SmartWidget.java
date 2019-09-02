package com.hhh.smartwidget;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.hhh.smartwidget.immersive.ImmersiveUtils;
import com.hhh.smartwidget.popup.DefaultPopupManager;
import com.hhh.smartwidget.popup.PopupInterface;
import com.hhh.smartwidget.toast.SmartToast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SmartWidget {

  private static Config sConfig;
  private static WeakReference<Activity> sCurrentActivity;

  private SmartWidget() {}

  public static void init(@NonNull Config config) {
    if (sConfig != null) {
      return;
    }
    sConfig = config;
    config.mApplication
        .registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
          @Override
          public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            sCurrentActivity = new WeakReference<>(activity);
            SmartToast.showPendingToast(activity);
            ImmersiveUtils.enterImmersive(activity);
          }

          @Override
          public void onActivityStarted(Activity activity) {}

          @Override
          public void onActivityResumed(Activity activity) {
            if (sCurrentActivity == null || sCurrentActivity.get() != activity) {
              sCurrentActivity = new WeakReference<>(activity);
            }
            SmartToast.showPendingToast(activity);
          }

          @Override
          public void onActivityPaused(Activity activity) {}

          @Override
          public void onActivityStopped(Activity activity) {}

          @Override
          public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

          @Override
          public void onActivityDestroyed(Activity activity) {
            if (sCurrentActivity != null && sCurrentActivity.get() == activity) {
              sCurrentActivity = null;
            }
            getPopupManager().onActivityDestroy(activity);
          }
        });
  }

  @NonNull
  public static Context getContext() {
    if (sCurrentActivity != null && sCurrentActivity.get() != null) {
      return sCurrentActivity.get();
    }
    return sConfig.mApplication;
  }

  @NonNull
  public static Application getApplication() {
    return sConfig.mApplication;
  }

  @Nullable
  public static Activity getCurrentActivity() {
    return sCurrentActivity == null ? null : sCurrentActivity.get();
  }

  @NonNull
  public static PopupInterface.PopupManager getPopupManager() {
    return sConfig.mPopupManager;
  }

  @NonNull
  public static SmartToast.Builder getBuilder() {
    return sConfig.mBuilder.clone();
  }

  public static class Config {

    private final Application mApplication;
    private PopupInterface.PopupManager mPopupManager = new DefaultPopupManager();
    private SmartToast.Builder mBuilder = new SmartToast.Builder();

    public Config(@NonNull Application application) {
      mApplication = application;
    }

    public Config setPopupManager(@NonNull PopupInterface.PopupManager popupManager) {
      mPopupManager = popupManager;
      return this;
    }

    public Config setBuilder(@NonNull SmartToast.Builder builder) {
      mBuilder = builder;
      return this;
    }
  }
}
