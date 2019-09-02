package com.hhh.smartwidget.toast;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.SmartWidget;
import com.hhh.smartwidget.WidgetUtils;
import com.hhh.smartwidget.popup.PopupInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class ToastFactory {

  private ToastFactory() {}

  @NonNull
  public static SmartToast info(@StringRes int textRes) {
    return info(WidgetUtils.getText(textRes));
  }

  @NonNull
  public static SmartToast info(@StringRes int textRes, Object... formatArgs) {
    return info(WidgetUtils.getString(textRes, formatArgs));
  }

  @NonNull
  public static SmartToast info(@NonNull CharSequence text) {
    return show(text, null);
  }

  @NonNull
  public static SmartToast notify(@StringRes int textRes) {
    return notify(WidgetUtils.getText(textRes));
  }

  @NonNull
  public static SmartToast notify(@StringRes int textRes, Object... formatArgs) {
    return notify(WidgetUtils.getString(textRes, formatArgs));
  }

  @NonNull
  public static SmartToast notify(@NonNull CharSequence text) {
    return show(text, WidgetUtils.getDrawable(R.drawable.toast_success));
  }

  @NonNull
  public static SmartToast alert(@StringRes int textRes) {
    return alert(WidgetUtils.getText(textRes));
  }

  @NonNull
  public static SmartToast alert(@StringRes int textRes, Object... formatArgs) {
    return alert(WidgetUtils.getString(textRes, formatArgs));
  }

  @NonNull
  public static SmartToast alert(@NonNull CharSequence text) {
    return show(text, WidgetUtils.getDrawable(R.drawable.toast_error));
  }

  @NonNull
  public static SmartToast show(@NonNull CharSequence text, @Nullable Drawable iconDrawable) {
    return SmartToast.show(SmartWidget.getBuilder().setText(text).setIcon(iconDrawable));
  }

  public static PopupInterface.OnAnimatorCallback getDefaultInAnimator() {
    return (toastView, listener) -> {
      ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(toastView, View.ALPHA, 0f, 1f);
      ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(toastView, View.SCALE_X, 0.9f, 1.0f);
      ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(toastView, View.SCALE_Y, 0.9f, 1.0f);
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
      animatorSet.setDuration(300L);
      animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
      animatorSet.addListener(listener);
      animatorSet.start();
    };
  }

  public static PopupInterface.OnAnimatorCallback getDefaultOutAnimator() {
    return (toastView, listener) -> {
      ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(toastView, View.ALPHA, 1f, 0f);
      ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(toastView, View.SCALE_X, 1f, 0.9f);
      ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(toastView, View.SCALE_Y, 1f, 0.9f);
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
      animatorSet.setDuration(240L);
      animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
      animatorSet.addListener(listener);
      animatorSet.start();
    };
  }
}
