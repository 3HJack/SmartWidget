package com.hhh.smartwidget.popup;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class PopupInterface {

  public static final String POPUP_TYPE_POPUP = "popup_type_popup";
  public static final String POPUP_TYPE_DIALOG = "popup_type_dialog";
  public static final String POPUP_TYPE_BUBBLE = "popup_type_bubble";
  public static final String POPUP_TYPE_INPUT = "popup_type_input";

  public static final int CLOSE_TYPE_AUTO = 0; // 程序自动关闭
  public static final int CLOSE_TYPE_BACK = 1; // back键关闭
  public static final int CLOSE_TYPE_OUTSIDE = 2; // 点击外部区域关闭
  public static final int CLOSE_TYPE_NEGATIVE = 3; // 其它一切消极关闭
  public static final int CLOSE_TYPE_POSITIVE = 4; // 其它一切积极关闭

  public static final OnVisibilityListener EMPTY_VISIBILITY_LISTENER =
      new OnVisibilityListener() {};

  private PopupInterface() {}

  public enum Excluded {
    NOT_AGAINST, // 不被任何弹窗排斥
    SAME_TYPE, // 被同类型弹窗排斥
    ALL_TYPE // 被所有弹窗排斥
  }

  public interface OnViewStateCallback {
    @NonNull
    View onCreateView(@NonNull Popup popup, @NonNull LayoutInflater inflater,
        @NonNull ViewGroup container, @Nullable Bundle bundle);

    default void onDestroyView(@NonNull Popup popup) {}
  }

  public interface OnVisibilityListener {
    default void onShow(@NonNull Popup popup) {}

    default void onDismiss(@NonNull Popup popup, int dismissType) {}

    /**
     * 由于优先级限制，弹窗排队等待展示.
     */
    default void onPending(@NonNull Popup popup) {}

    /**
     * 没有展示就被扔掉，可能发生在页面销毁时，或者还没有展示就调用 dismiss.
     */
    default void onDiscard(@NonNull Popup popup) {}
  }

  public interface OnCancelListener {
    void onCancel(@NonNull Popup popup, int cancelType);
  }

  public interface OnAnimatorCallback {
    void onStartAnimator(@NonNull View view, @Nullable Animator.AnimatorListener animatorListener);
  }

  /**
   * 处理弹窗优先级，Popup 本身不处理优先级和排队.
   *
   * {@link DefaultPopupManager} 提供了默认的排队策略.
   */
  public interface PopupManager {

    /**
     * popup 是否可以展示，所有弹窗在展示前都会调用该方法进行判断.
     *
     * @param activity
     * @param popup
     * @return true or false
     */
    boolean enableShowNow(@NonNull Activity activity, @NonNull Popup popup);

    /**
     * 弹窗展示的回调，和 {@link #onPopupDismiss(Activity, Popup)} 成对.
     */
    void onPopupShow(@NonNull Activity activity, @NonNull Popup popup);

    /**
     * 弹窗消失的回调，和 {@link #onPopupShow(Activity, Popup)} 成对.
     */
    void onPopupDismiss(@NonNull Activity activity, @NonNull Popup popup);

    /**
     * 进入队列回调
     *
     * @see #enableShowNow(Activity, Popup) 确定是否进队.
     */
    void onPopupPending(@NonNull Activity activity, @NonNull Popup popup);

    /**
     * 排队中的弹窗调用 dismiss 取消展示
     *
     * @see #enableShowNow(Activity, Popup) 确定是否进队.
     */
    void onPopupDiscard(@NonNull Activity activity, @NonNull Popup popup);

    void onActivityDestroy(@NonNull Activity activity);
  }

  public static class OnViewStateCallbackInflateAdapter implements OnViewStateCallback {
    private final @LayoutRes int mLayoutRes;

    public OnViewStateCallbackInflateAdapter(@LayoutRes int layoutRes) {
      mLayoutRes = layoutRes;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Popup popup, @NonNull LayoutInflater inflater,
        @NonNull ViewGroup container, @Nullable Bundle bundle) {
      View view = inflater.inflate(mLayoutRes, container, false);
      onViewCreated(popup, view);
      return view;
    }

    protected void onViewCreated(Popup popup, View view) {}
  }
}
