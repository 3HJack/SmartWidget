package com.hhh.smartwidget.dialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.dialog.adapter.ListButtonAdapter;
import com.hhh.smartwidget.dialog.adapter.ListMultiAdapter;
import com.hhh.smartwidget.dialog.adapter.ListSimpleAdapter;
import com.hhh.smartwidget.dialog.adapter.ListSingleAdapter;
import com.hhh.smartwidget.dialog.adapter.ListSingleButtonAdapter;
import com.hhh.smartwidget.dialog.adjust.AdjustGeneralStyle;
import com.hhh.smartwidget.dialog.adjust.AdjustTitleLayout;
import com.hhh.smartwidget.popup.PopupInterface;

import androidx.annotation.NonNull;

public class DialogBuilderFactory {

  @NonNull
  public static <T extends SmartDialog.Builder> T applySimpleDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustTitleLayout())
        .addAdjustStyles(new AdjustGeneralStyle())
        .setOnViewStateCallback(
            new PopupInterface.OnViewStateCallbackInflateAdapter(R.layout.dialog_layout_simple));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applyInputDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustTitleLayout())
        .addAdjustStyles(new AdjustGeneralStyle())
        .setOnViewStateCallback(
            new PopupInterface.OnViewStateCallbackInflateAdapter(R.layout.dialog_layout_input));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applySmallIconDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustTitleLayout())
        .addAdjustStyles(new AdjustGeneralStyle())
        .setOnViewStateCallback(new PopupInterface.OnViewStateCallbackInflateAdapter(
            R.layout.dialog_layout_small_icon));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applyBigIconDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustTitleLayout())
        .addAdjustStyles(new AdjustGeneralStyle())
        .setOnViewStateCallback(
            new PopupInterface.OnViewStateCallbackInflateAdapter(R.layout.dialog_layout_big_icon));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applyListDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustGeneralStyle())
        .setAdapter(new ListSimpleAdapter(builder))
        .setListItemLayout(R.layout.dialog_list_item_view)
        .setOnViewStateCallback(
            new PopupInterface.OnViewStateCallbackInflateAdapter(R.layout.dialog_layout_list));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applyListMultiDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustGeneralStyle())
        .setAdapter(new ListMultiAdapter(builder))
        .setListItemLayout(R.layout.dialog_list_multi_item_view)
        .setOnViewStateCallback(
            new PopupInterface.OnViewStateCallbackInflateAdapter(R.layout.dialog_layout_list));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applyListButtonDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustGeneralStyle())
        .setAdapter(new ListButtonAdapter(builder))
        .setListItemLayout(R.layout.dialog_list_button_item_view)
        .setCanceledOnTouchOutside(false)
        .setOnViewStateCallback(new PopupInterface.OnViewStateCallbackInflateAdapter(
            R.layout.dialog_layout_list_button));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applyListSingleDialogStyle(@NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustGeneralStyle())
        .setAdapter(new ListSingleAdapter(builder))
        .setListItemLayout(R.layout.dialog_list_single_item_view)
        .setOnViewStateCallback(new PopupInterface.OnViewStateCallbackInflateAdapter(
            R.layout.dialog_layout_list_single));
  }

  @NonNull
  public static <T extends SmartDialog.Builder> T applyListSingleButtonDialogStyle(
      @NonNull T builder) {
    return builder
        .addAdjustStyles(new AdjustGeneralStyle())
        .setAdapter(new ListSingleButtonAdapter(builder))
        .setListItemLayout(R.layout.dialog_list_single_button_item_view)
        .setOnViewStateCallback(
            new PopupInterface.OnViewStateCallbackInflateAdapter(R.layout.dialog_layout_list));
  }

  public static PopupInterface.OnAnimatorCallback createBottomSlideInAnimator(int duration) {
    return (view, animatorListener) -> {
      Animator animator = ObjectAnimator
          .ofFloat(view, View.TRANSLATION_Y, view.getMeasuredHeight(), 0).setDuration(duration);
      if (animatorListener != null) {
        animator.addListener(animatorListener);
      }
      animator.start();
    };
  }

  public static PopupInterface.OnAnimatorCallback createBottomSlideOutAnimator(int duration) {
    return (view, animatorListener) -> {
      Animator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getMeasuredHeight())
          .setDuration(duration);
      if (animatorListener != null) {
        animator.addListener(animatorListener);
      }
      animator.start();
    };
  }

  @NonNull
  public static PopupInterface.OnAnimatorCallback getDefaultInAnimator() {
    return (view, animatorListener) -> {
      ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
      ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.8f, 1f);
      ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.8f, 1f);
      ObjectAnimator parentViewAlphaAnimator =
          ObjectAnimator.ofFloat((View) view.getParent(), View.ALPHA, 0f, 1f);
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator,
          parentViewAlphaAnimator);
      animatorSet.setDuration(300L);
      animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
      if (animatorListener != null) {
        animatorSet.addListener(animatorListener);
      }
      animatorSet.start();
    };
  }

  @NonNull
  public static PopupInterface.OnAnimatorCallback getDefaultOutAnimator() {
    return (view, animatorListener) -> {
      ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
      ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.8f);
      ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.8f);
      ObjectAnimator parentViewAlphaAnimator =
          ObjectAnimator.ofFloat((View) view.getParent(), View.ALPHA, 1f, 0f);
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator,
          parentViewAlphaAnimator);
      animatorSet.setDuration(240L);
      animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
      if (animatorListener != null) {
        animatorSet.addListener(animatorListener);
      }
      animatorSet.start();
    };
  }
}
