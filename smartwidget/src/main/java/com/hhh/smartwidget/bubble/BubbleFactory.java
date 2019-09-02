package com.hhh.smartwidget.bubble;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.popup.PopupInterface;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

public class BubbleFactory {
  private BubbleFactory() {}

  @NonNull
  public static Bubble showLeftWhiteBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.LEFT),
        R.layout.bubble_layout_white_left);
  }

  @NonNull
  public static Bubble showTopWhiteBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.TOP),
        R.layout.bubble_layout_white_top);
  }

  @NonNull
  public static Bubble showRightWhiteBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.RIGHT),
        R.layout.bubble_layout_white_right);
  }

  @NonNull
  public static Bubble showBottomWhiteBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.BOTTOM),
        R.layout.bubble_layout_white_bottom);
  }

  @NonNull
  public static Bubble showLeftBlackBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.LEFT),
        R.layout.bubble_layout_black_left);
  }

  @NonNull
  public static Bubble showTopBlackBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.TOP),
        R.layout.bubble_layout_black_top);
  }

  @NonNull
  public static Bubble showRightBlackBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.RIGHT),
        R.layout.bubble_layout_black_right);
  }

  @NonNull
  public static Bubble showBottomBlackBubble(@NonNull Bubble.Builder builder) {
    return showBubble(builder.setPosition(BubbleInterface.Position.BOTTOM),
        R.layout.bubble_layout_black_bottom);
  }

  @NonNull
  public static Bubble showBubble(@NonNull Bubble.Builder builder, @LayoutRes int layoutRes) {
    return builder
        .setOnViewStateCallback(
            (popup, inflater, container, bundle) -> inflater.inflate(layoutRes, container, false))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  @NonNull
  public static PopupInterface.OnAnimatorCallback getDefaultInAnimator() {
    return (view, animatorListener) -> {
      ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
      ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.8f, 1f);
      ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.8f, 1f);
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
      animatorSet.setDuration(300L);
      animatorSet.setInterpolator(new OvershootInterpolator(1.74f));
      if (animatorListener != null) {
        animatorSet.addListener(animatorListener);
      }
      setPivot(view);
      animatorSet.start();
    };
  }

  @NonNull
  public static PopupInterface.OnAnimatorCallback getDefaultOutAnimator() {
    return (view, animatorListener) -> {
      ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
      ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.8f);
      ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.8f);
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
      animatorSet.setDuration(240L);
      animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
      if (animatorListener != null) {
        animatorSet.addListener(animatorListener);
      }
      setPivot(view);
      animatorSet.start();
    };
  }

  private static void setPivot(View view) {
    View arrowView = view.findViewById(R.id.arrow);
    if (arrowView == null || !(view instanceof LinearLayout)) {
      return;
    }
    if (((LinearLayout) view).getOrientation() == LinearLayout.VERTICAL) {
      view.setPivotX(view.getPivotX() + arrowView.getTranslationX());
      view.setPivotY(arrowView.getY() + arrowView.getHeight());
    } else {
      view.setPivotX(arrowView.getX() + arrowView.getWidth());
      view.setPivotY(view.getPivotY() + arrowView.getTranslationY());
    }
  }
}
