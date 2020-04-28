package com.hhh.smartwidget.popup;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.UiThread;

import com.hhh.smartwidget.SmartWidget;
import com.hhh.smartwidget.WidgetUtils;

public class Popup {

  private static final List<View> FOCUSABLE_VIEW_LIST = new ArrayList<>();

  protected final Builder mBuilder;
  protected final Runnable mAutoDismiss;
  protected final PopupRootLayout mRootLayout;
  protected final View.OnKeyListener mOnKeyListener;

  protected View mPopupView;
  protected boolean mShowing;
  protected boolean mCanceled;

  protected Popup(Builder builder) {
    mBuilder = builder;
    mAutoDismiss = () -> dismiss(PopupInterface.CLOSE_TYPE_AUTO);
    mRootLayout = new PopupRootLayout(mBuilder.mActivity);
    mRootLayout.setChildMaxHeight(mBuilder.mMaxHeight).setChildMaxWidth(mBuilder.mMaxWidth)
        .setPadding(0, mBuilder.mTopPadding, 0, mBuilder.mBottomPadding);
    mRootLayout.setBackground(mBuilder.mBackground);
    mOnKeyListener = (v, keyCode, event) -> {
      if (!mBuilder.mCancelable) {
        return true;
      }
      if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN
          && isShowing()) {
        cancelPopup(PopupInterface.CLOSE_TYPE_BACK);
        return true;
      }
      return false;
    };
  }

  public static boolean isPermanentPopup(@NonNull Popup popup) {
    return !popup.mBuilder.mCanceledOnTouchOutside && popup.mBuilder.mPenetrateOutsideTouchEvent;
  }

  /**
   * 焦点的自动分发有时失效，可以调用此方法，相应的记得调用 {@link #removeFocusableView} 方法
   */
  public static void addFocusableView(@NonNull View view) {
    if (!FOCUSABLE_VIEW_LIST.contains(view)) {
      FOCUSABLE_VIEW_LIST.add(view);
    }
  }

  public static void removeFocusableView(@NonNull View view) {
    FOCUSABLE_VIEW_LIST.remove(view);
  }

  @NonNull
  public Activity getContext() {
    return mBuilder.mActivity;
  }

  /**
   * 所有弹窗都可能会排队，而不是马上展示。
   * 当弹窗真正展示的时候，getPopupView 才有意义，需要在 onShowPopup 或者
   * {@link PopupInterface.OnVisibilityListener#onShow(Popup)} 调用。
   *
   * @return popup view.
   */
  @Nullable
  public View getPopupView() {
    return mPopupView;
  }

  @Nullable
  public Object getTag() {
    return mBuilder.mTag;
  }

  @NonNull
  public String getPopupType() {
    return mBuilder.mPopupType;
  }

  @NonNull
  public PopupInterface.Excluded getExcluded() {
    return mBuilder.mExcluded;
  }

  public boolean isShowing() {
    return mShowing;
  }

  /**
   * 展示弹窗，不一定马上展示，可能排队.
   *
   * @param <T>
   */
  @UiThread
  public final <T extends Popup> T show() {
    checkLegality();
    if (mBuilder.mActivity.isFinishing()) {
      discard();
      return (T) this;
    }
    if (isShowing()) {
      return (T) this;
    }
    if (getPopupManager().enableShowNow(mBuilder.mActivity, this)) {
      createPopup();
    } else {
      getPopupManager().onPopupPending(mBuilder.mActivity, this);
      if (mBuilder.mOnVisibilityListener != null) {
        mBuilder.mOnVisibilityListener.onPending(this);
      }
    }
    return (T) this;
  }

  /**
   * 关闭弹窗.
   * 和 show 的语义对称，show 展示或者入队，dismiss 消失或者从队列移除.
   *
   * @param dismissType
   */
  @UiThread
  public final void dismiss(int dismissType) {
    if (!isShowing()) {
      discard();
      return;
    }
    if (!WidgetUtils.isMainThread()) {
      throw new RuntimeException("Must be called on the main thread!!!");
    }
    dismissPopup(dismissType);
  }

  public final void discard() {
    if (isShowing()) {
      return;
    }
    getPopupManager().onPopupDiscard(mBuilder.mActivity, this);
    if (mBuilder.mOnVisibilityListener != null) {
      mBuilder.mOnVisibilityListener.onDiscard(this);
    }
  }

  public void setCancelable(boolean cancelable) {
    mBuilder.mCancelable = cancelable;
  }

  public void setCanceledOnTouchOutside(boolean cancelable) {
    if (cancelable && !mBuilder.mCancelable) {
      mBuilder.mCancelable = true;
    }
    mBuilder.mCanceledOnTouchOutside = cancelable;
  }

  /**
   * 如果有弹窗外的view抢占了焦点，需调用此方法，否则无法拦截back事件
   */
  public void interceptBackEvent(@NonNull View view) {
    if (view instanceof ViewGroup) {
      setKeyListener((ViewGroup) view);
    } else {
      view.setOnKeyListener(mOnKeyListener);
    }
  }

  protected void onShowPopup(@Nullable Bundle bundle) {}

  protected void onDismissPopup(@Nullable Bundle bundle) {}

  protected final void cancelPopup(int cancelType) {
    dismiss(cancelType);
    if (mBuilder.mOnCancelListener == null || mCanceled) {
      return;
    }
    mCanceled = true;
    mBuilder.mOnCancelListener.onCancel(this, cancelType);
  }

  @Nullable
  protected final <T extends View> T findViewById(@IdRes int id) {
    return mPopupView.findViewById(id);
  }

  private void checkLegality() {
    if (mBuilder.mActivity == null || mBuilder.mOnViewStateCallback == null) {
      throw new IllegalArgumentException(
          "mBuilder.mActivity and mBuilder.mOnViewStateCallback cannot be null!!!");
    }
    if (!WidgetUtils.isMainThread()) {
      throw new RuntimeException("Must be called on the main thread!!!");
    }
  }

  private void createPopup() {
    mShowing = true;
    mCanceled = false;
    mPopupView = mBuilder.mOnViewStateCallback.onCreateView(this,
        LayoutInflater.from(mBuilder.mActivity), mRootLayout, mBuilder.mBundle);
    if (mPopupView == mRootLayout) {
      if (mRootLayout.getChildCount() != 1) {
        throw new RuntimeException("mRootLayout has one and only one child View!!!");
      }
      mPopupView = mRootLayout.getChildAt(0);
    } else {
      mRootLayout.addView(mPopupView);
    }

    if (!mBuilder.mIsAddToWindow) {
      ((ViewGroup) mBuilder.mActivity.getWindow().getDecorView()).addView(mRootLayout,
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    } else {
      WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
      layoutParams.copyFrom(mBuilder.mActivity.getWindow().getAttributes());
      layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
      layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
      layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
      layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
      layoutParams.format = PixelFormat.TRANSLUCENT;
      layoutParams.gravity = Gravity.CENTER;
      mBuilder.mActivity.getWindowManager().addView(mRootLayout, layoutParams);
    }

    FOCUSABLE_VIEW_LIST.add(mRootLayout);
    getPopupManager().onPopupShow(mBuilder.mActivity, this);
    onShowPopup(mBuilder.mBundle);
    if (mBuilder.mOnVisibilityListener != null) {
      mBuilder.mOnVisibilityListener.onShow(this);
    }
    observerViewProperty();
  }

  @SuppressLint("ClickableViewAccessibility")
  private void observerViewProperty() {
    mPopupView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            mPopupView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if (mBuilder.mInAnimatorCallback != null) {
              mBuilder.mInAnimatorCallback.onStartAnimator(mPopupView,
                  new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                      autoDismiss();
                    }
                  });
            } else {
              autoDismiss();
            }
          }
        });
    mRootLayout.setOnTouchListener((v, event) -> {
      if (isPermanentPopup(Popup.this)) {
        mBuilder.mActivity.dispatchTouchEvent(event);
        return false;
      }
      if (!mBuilder.mCancelable || !mBuilder.mCanceledOnTouchOutside) {
        return true;
      }
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        cancelPopup(PopupInterface.CLOSE_TYPE_OUTSIDE);
        return !mBuilder.mPenetrateOutsideTouchEvent;
      }
      return false;
    });
    mRootLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(View v) {}

      @Override
      public void onViewDetachedFromWindow(View v) {
        if (isShowing()) {
          dismiss(PopupInterface.CLOSE_TYPE_AUTO);
        }
      }
    });
    mRootLayout.setFocusable(true);
    mRootLayout.setFocusableInTouchMode(true);
    mRootLayout.requestFocus();
    setKeyListener(mRootLayout);
  }

  private void setKeyListener(ViewGroup viewGroup) {
    viewGroup.setOnKeyListener(mOnKeyListener);
    int childCount = viewGroup.getChildCount();
    for (int i = 0; i < childCount; ++i) {
      View view = viewGroup.getChildAt(i);
      if (view instanceof ViewGroup) {
        setKeyListener((ViewGroup) view);
      } else {
        view.setOnKeyListener(mOnKeyListener);
      }
    }
  }

  private void autoDismiss() {
    if (mBuilder.mShowDuration > 0) {
      mPopupView.postDelayed(mAutoDismiss, mBuilder.mShowDuration);
    }
  }

  private void dismissPopup(int dismissType) {
    mShowing = false;
    getPopupManager().onPopupDismiss(mBuilder.mActivity, this);
    mPopupView.removeCallbacks(mAutoDismiss);
    if (mBuilder.mOutAnimatorCallback != null) {
      mBuilder.mOutAnimatorCallback.onStartAnimator(mPopupView, new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          removeView(dismissType);
        }
      });
    } else {
      removeView(dismissType);
    }
  }

  private void removeView(int dismissType) {
    if (mBuilder.mOnVisibilityListener != null) {
      mBuilder.mOnVisibilityListener.onDismiss(this, dismissType);
    }
    onDismissPopup(mBuilder.mBundle);
    mBuilder.mOnViewStateCallback.onDestroyView(this);
    if (!mBuilder.mIsAddToWindow) {
      ViewParent parent = mRootLayout.getParent();
      if (parent instanceof ViewGroup) {
        ((ViewGroup) parent).removeView(mRootLayout);
      }
    } else {
      try {
        mBuilder.mActivity.getWindowManager().removeViewImmediate(mRootLayout);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    FOCUSABLE_VIEW_LIST.remove(mRootLayout);
    if (!FOCUSABLE_VIEW_LIST.isEmpty()) {
      FOCUSABLE_VIEW_LIST.get(FOCUSABLE_VIEW_LIST.size() - 1).requestFocus();
    }
  }

  private PopupInterface.PopupManager getPopupManager() {
    return SmartWidget.getPopupManager();
  }

  public boolean isAdded() {
    return mRootLayout.getParent() != null;
  }

  public static class Builder {
    protected final Activity mActivity;

    protected boolean mCancelable = true;
    protected boolean mCanceledOnTouchOutside = true;
    protected boolean mPenetrateOutsideTouchEvent;
    protected boolean mIsAddToWindow;
    protected long mShowDuration = -1L;

    protected int mMaxHeight = Integer.MAX_VALUE;
    protected int mMaxWidth = Integer.MAX_VALUE;
    protected int mTopPadding;
    protected int mBottomPadding;

    protected Drawable mBackground;
    protected Bundle mBundle;
    protected Object mTag;

    protected String mPopupType = PopupInterface.POPUP_TYPE_POPUP;
    protected PopupInterface.Excluded mExcluded = PopupInterface.Excluded.NOT_AGAINST;
    protected PopupInterface.OnViewStateCallback mOnViewStateCallback;
    protected PopupInterface.OnVisibilityListener mOnVisibilityListener;
    protected PopupInterface.OnCancelListener mOnCancelListener;
    protected PopupInterface.OnAnimatorCallback mInAnimatorCallback;
    protected PopupInterface.OnAnimatorCallback mOutAnimatorCallback;

    public Builder(@NonNull Activity activity) {
      mActivity = activity;
      mTopPadding = WidgetUtils.getStatusBarHeight(activity);
      if (!WidgetUtils.isLandscape()) {
        mBottomPadding = WidgetUtils.getNavigationBarHeight(activity);
      }
    }

    public Popup build() {
      return new Popup(this);
    }

    @UiThread
    public final <T extends Popup> T show(@NonNull PopupInterface.OnVisibilityListener listener) {
      Popup popup = build();
      popup.mBuilder.mOnVisibilityListener = listener;
      return popup.show();
    }

    /**
     * 必须设置,且必须在回调中返回有效的View
     */
    public <T extends Builder> T setOnViewStateCallback(
        @NonNull PopupInterface.OnViewStateCallback onViewStateCallback) {
      mOnViewStateCallback = onViewStateCallback;
      return (T) this;
    }

    /**
     * 默认值：true
     * true：可back键销毁弹窗，是否可点击外部区域销毁弹窗需要看 {@link #mCanceledOnTouchOutside}
     * false：不可back键销毁弹窗，不可点击外部区域销毁弹窗
     */
    public <T extends Builder> T setCancelable(boolean cancelable) {
      mCancelable = cancelable;
      return (T) this;
    }

    /**
     * 默认值：true
     * true：可点击外部区域销毁弹窗,前提是 {@link #mCancelable} 必须为true
     * false：不可点击外部区域销毁弹窗
     */
    public <T extends Builder> T setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
      mCanceledOnTouchOutside = canceledOnTouchOutside;
      return (T) this;
    }

    /**
     * 展示时间
     * 默认值：-1L,表示必须由用户手动触发dismiss
     */
    public <T extends Builder> T setShowDuration(@IntRange(from = 1L) long showDuration) {
      mShowDuration = showDuration;
      return (T) this;
    }

    /**
     * 默认值：false
     * true：外部区域的触摸事件都将透传给下面的view,前提是 {@link #mCancelable} 和 {@link #mCanceledOnTouchOutside}
     * 必须为true
     * false：外部区域拦截一切触摸事件
     */
    public <T extends Builder> T setPenetrateOutsideTouchEvent(boolean penetrateOutsideTouchEvent) {
      mPenetrateOutsideTouchEvent = penetrateOutsideTouchEvent;
      return (T) this;
    }

    /**
     * 默认值：false
     * true：将View添加给Window
     * false：默认处理，将View添加给decorView
     */
    public <T extends Builder> T setAddToWindow(boolean isAddToWindow) {
      mIsAddToWindow = isAddToWindow;
      return (T) this;
    }

    /**
     * {@link #mRootLayout} 的顶部padding，默认值为状态栏高度
     */
    public Builder setTopPadding(@Px int topPadding) {
      mTopPadding = topPadding;
      return this;
    }

    /**
     * {@link #mRootLayout} 的底部padding
     * 1.如果存在虚拟导航栏，默认值为虚拟导航栏的高度
     * 2.不存在虚拟导航栏，默认值为0
     */
    public Builder setBottomPadding(@Px int bottomPadding) {
      mBottomPadding = bottomPadding;
      return this;
    }

    /**
     * 默认值：{@link Integer#MAX_VALUE}，表示不限制高度
     * 设置弹窗的最大高度
     */
    public <T extends Builder> T setMaxHeight(@Px int maxHeight) {
      mMaxHeight = maxHeight;
      return (T) this;
    }

    /**
     * 默认值：{@link Integer#MAX_VALUE}，表示不限制宽度
     * 设置弹窗的最大宽度
     */
    public <T extends Builder> T setMaxWidth(@Px int maxWidth) {
      mMaxWidth = maxWidth;
      return (T) this;
    }

    /**
     * 外部区域背景色，默认透明
     */
    public <T extends Builder> T setBackground(@Nullable Drawable background) {
      mBackground = background;
      return (T) this;
    }

    /**
     * 用来传输一些额外的数据，将通过
     * {@link #onShowPopup(Bundle),#onDismissPopup(Bundle), PopupInterface.OnViewStateCallback}
     * 回调给用户
     */
    public <T extends Builder> T setBundle(@Nullable Bundle bundle) {
      mBundle = bundle;
      return (T) this;
    }

    /**
     * 用来标识一个Popup
     */
    public <T extends Builder> T setTag(@Nullable Object tag) {
      mTag = tag;
      return (T) this;
    }

    /**
     * 弹窗类型，主要用来弹窗互斥，作者目前定义了三种类型，开发者可自由发挥
     */
    public <T extends Builder> T setPopupType(@NonNull String popupType) {
      mPopupType = popupType;
      return (T) this;
    }

    /**
     * 排斥类型
     */
    public <T extends Builder> T setExcluded(@NonNull PopupInterface.Excluded excluded) {
      mExcluded = excluded;
      return (T) this;
    }

    /**
     * 用户通过触摸外部区域或者back键等方式销毁弹窗的回调
     */
    public <T extends Builder> T setOnCancelListener(
        @Nullable PopupInterface.OnCancelListener onCancelListener) {
      mOnCancelListener = onCancelListener;
      return (T) this;
    }

    /**
     * 弹窗展示动画
     */
    public <T extends Builder> T setInAnimatorCallback(
        @Nullable PopupInterface.OnAnimatorCallback inAnimatorCallback) {
      mInAnimatorCallback = inAnimatorCallback;
      return (T) this;
    }

    /**
     * 弹窗销毁动画
     */
    public <T extends Builder> T setOutAnimatorCallback(
        @Nullable PopupInterface.OnAnimatorCallback outAnimatorCallback) {
      mOutAnimatorCallback = outAnimatorCallback;
      return (T) this;
    }
  }
}
