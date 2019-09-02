package com.hhh.smartwidget;

import java.lang.reflect.Field;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public final class WidgetUtils {
  private static final Handler sHandler = new Handler(Looper.getMainLooper());

  private WidgetUtils() {}

  public static void runOnUIThread(@NonNull Runnable runnable) {
    if (isMainThread()) {
      runnable.run();
    } else {
      sHandler.post(runnable);
    }
  }

  public static void runOnUIThread(@NonNull Runnable runnable, long delayed) {
    sHandler.postDelayed(runnable, delayed);
  }

  public static void removeCallbacks(@NonNull Runnable runnable) {
    sHandler.removeCallbacks(runnable);
  }

  public static void showKeyboard(@NonNull EditText editText) {
    showKeyboard(editText, 0L);
  }

  public static void showKeyboard(@NonNull EditText editText, long delayed) {
    runOnUIThread(() -> {
      editText.requestFocus();
      InputMethodManager imm = (InputMethodManager) SmartWidget.getContext()
          .getSystemService(Context.INPUT_METHOD_SERVICE);
      if (imm != null) {
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
      }
    }, delayed);
  }

  public static boolean hideSoftInput() {
    Activity activity = SmartWidget.getCurrentActivity();
    if (activity != null) {
      View focusView = activity.getCurrentFocus();
      if (focusView != null) {
        return hideSoftInput(focusView.getWindowToken());
      } else {
        return false;
      }
    }
    return false;
  }

  public static boolean hideSoftInput(@NonNull Window window) {
    View focusView = window.getCurrentFocus();
    if (focusView != null) {
      return hideSoftInput(focusView.getWindowToken());
    }
    return false;
  }

  public static boolean hideSoftInput(@Nullable IBinder windowToken) {
    if (windowToken == null) {
      return false;
    }
    InputMethodManager imm = ((InputMethodManager) SmartWidget.getContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE));
    if (imm != null) {
      return imm.hideSoftInputFromWindow(windowToken, 0);
    }
    return false;
  }

  @NonNull
  public static Resources getResources() {
    return SmartWidget.getContext().getResources();
  }

  public static int getDimensionPixelSize(@DimenRes int id) {
    return getResources().getDimensionPixelSize(id);
  }

  public static boolean isMainThread() {
    return Looper.getMainLooper() == Looper.myLooper();
  }

  public static float getFloatDimensionSize(@DimenRes int id) {
    TypedValue outValue = new TypedValue();
    getResources().getValue(id, outValue, true);
    return outValue.getFloat();
  }

  @NonNull
  public static CharSequence getText(@StringRes int resId) {
    return getResources().getText(resId);
  }

  @NonNull
  public static String getString(@StringRes int resId, Object... formatArgs) {
    return getResources().getString(resId, formatArgs);
  }

  @NonNull
  public static Drawable getDrawable(@DrawableRes int drawableId) {
    return getResources().getDrawable(drawableId);
  }

  public static int dip2px(float dpValue) {
    return (int) (dpValue * getResources().getDisplayMetrics().density + 0.5f);
  }

  /**
   * 需要在DecorView layout完成以后才能获取到正确的值
   */
  public static int getWindowWidth(@NonNull Activity activity) {
    return activity.getWindow().getDecorView().getWidth();
  }

  /**
   * 需要在DecorView layout完成以后才能获取到正确的值
   */
  public static int getWindowHeight(@NonNull Activity activity) {
    return activity.getWindow().getDecorView().getHeight();
  }

  /**
   * 某种情况下 getChildFragmentManager 在子线程会崩溃，所以建议在
   * UI线程调用，否则可能拿不到顶部的 DialogFragment
   */
  @Nullable
  @UiThread
  public static DialogFragment getTopDialogFragment() {
    Context context = SmartWidget.getContext();
    if (!(context instanceof FragmentActivity)) {
      return null;
    }
    List<Fragment> fragmentList =
        ((FragmentActivity) context).getSupportFragmentManager().getFragments();
    DialogFragment dialogFragment = null;
    Fragment fragment;
    if (isMainThread()) {
      while (!fragmentList.isEmpty()) {
        fragment = fragmentList.get(fragmentList.size() - 1);
        if (fragment instanceof DialogFragment) {
          dialogFragment = (DialogFragment) fragment;
        }
        fragmentList = fragment.getChildFragmentManager().getFragments();
      }
    } else {
      fragment = fragmentList.isEmpty() ? null : fragmentList.get(fragmentList.size() - 1);
      if (fragment instanceof DialogFragment) {
        dialogFragment = (DialogFragment) fragment;
      }
    }
    return dialogFragment;
  }

  @Nullable
  public static ViewGroup getDialogFragmentContentView(@NonNull DialogFragment dialogFragment) {
    View view = dialogFragment.getView();
    return (view != null && view.getParent() != null) ? (ViewGroup) view.getParent() : null;
  }

  public static int getStatusBarHeight(@NonNull Activity activity) {
    SystemBarInfo statusBarInfo = getStatusBarInfo(activity);
    if (!statusBarInfo.mIsExist) {
      return 0;
    }
    if (statusBarInfo.mHeight > 0) {
      return statusBarInfo.mHeight;
    }
    int statusBarHeight = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      statusBarHeight = getDimensionPixelSize(resourceId);
    } else {
      try {
        @SuppressLint("PrivateApi")
        Class<?> c = Class.forName("com.android.internal.R$dimen");
        Field field = c.getField("status_bar_height");
        field.setAccessible(true);
        statusBarHeight =
            getDimensionPixelSize(Integer.parseInt(field.get(c.newInstance()).toString()));
      } catch (Throwable throwable) {}
    }
    if (statusBarHeight <= 0) {
      statusBarHeight = dip2px(25f);
    }
    return statusBarHeight;
  }

  public static int getNavigationBarHeight(@NonNull Activity activity) {
    SystemBarInfo navigationBarInfo = getNavigationBarInfo(activity);
    if (!navigationBarInfo.mIsExist) {
      return 0;
    }
    return navigationBarInfo.mHeight > 0
        ? navigationBarInfo.mHeight
        : getDimensionPixelSize(
            getResources().getIdentifier("navigation_bar_height", "dimen", "android"));
  }

  @NonNull
  public static SystemBarInfo getNavigationBarInfo(@NonNull Activity activity) {
    SystemBarInfo navigationBarInfo = new SystemBarInfo();
    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
    int childCount = decorView.getChildCount();
    for (int i = 0; i < childCount; ++i) {
      View childView = decorView.getChildAt(i);
      if (childView.getId() == android.R.id.navigationBarBackground) {
        navigationBarInfo.mIsExist = true;
        navigationBarInfo.mHeight = childView.getHeight();
        break;
      }
    }
    return navigationBarInfo;
  }

  @NonNull
  public static SystemBarInfo getStatusBarInfo(@NonNull Activity activity) {
    SystemBarInfo statusBarInfo = new SystemBarInfo();
    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
    int childCount = decorView.getChildCount();
    for (int i = 0; i < childCount; ++i) {
      View childView = decorView.getChildAt(i);
      if (childView.getId() == android.R.id.statusBarBackground) {
        statusBarInfo.mIsExist = true;
        statusBarInfo.mHeight = childView.getHeight();
        break;
      }
    }
    return statusBarInfo;
  }

  public static boolean isMIUI() {
    return Build.MANUFACTURER.equalsIgnoreCase("Xiaomi");
  }

  public static boolean isFlyme() {
    try {
      return Build.class.getMethod("hasSmartBar") != null;
    } catch (Exception e) {}
    return false;
  }

  public static boolean isFullScreen(@NonNull Window window) {
    return (window.getAttributes().flags
        & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
  }

  public static boolean isLandscape() {
    return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }
}
