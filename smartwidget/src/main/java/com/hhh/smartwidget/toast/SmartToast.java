package com.hhh.smartwidget.toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.SmartWidget;
import com.hhh.smartwidget.WidgetUtils;
import com.hhh.smartwidget.popup.PopupInterface;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class SmartToast {

  private static final int MSG_SHOW = 0;
  private static final int MSG_DISMISS = 1;
  private static final Handler sHandler;
  private static final List<Interceptor> sInterceptors = new ArrayList<>();
  private static WeakReference<SmartToast> sCurrentToast;
  private static long sResidueDuration = 1000L;

  static {
    sHandler = new Handler(Looper.getMainLooper(), message -> {
      switch (message.what) {
        case MSG_SHOW:
          ((SmartToast) message.obj).showView();
          return true;
        case MSG_DISMISS:
          ((SmartToast) message.obj).dismissView();
          return true;
        default:
          return false;
      }
    });
  }

  protected final Builder mBuilder;
  protected final ToastManager.Callback mManagerCallback;
  protected View mToastView;
  protected ViewGroup mRootLayout;
  protected long mStartShowTime;

  protected SmartToast(Builder builder) {
    mBuilder = builder;
    mManagerCallback = new ToastManager.Callback() {
      @Override
      public void show() {
        sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, SmartToast.this));
      }

      @Override
      public void dismiss() {
        sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, SmartToast.this));
      }
    };
    initView();
  }

  /**
   * ArrayList 是非线程安全的，开发者自己注意多线程问题
   */
  public static boolean addInterceptor(@NonNull Interceptor interceptor) {
    if (sInterceptors.contains(interceptor)) {
      return false;
    }
    return sInterceptors.add(interceptor);
  }

  /**
   * ArrayList 是非线程安全的，开发者自己注意多线程问题
   */
  public static boolean removeInterceptor(@NonNull Interceptor interceptor) {
    return sInterceptors.remove(interceptor);
  }

  public static void setResidueDuration(long residueDuration) {
    sResidueDuration = residueDuration;
  }

  public static void showPendingToast(Activity activity) {
    SmartToast toast = getCurrentToast();
    if (toast == null) {
      return;
    }
    long residueDuration = toast.getNeedShowDuration() - toast.getShownDuration();
    // 差值需要大于sResidueDuration，是觉得如果展示时间差的不多的话，也没必要在下个Activity继续展示
    if (toast.getContext() != activity && residueDuration > sResidueDuration) {
      Builder builder = toast.getBuilder();
      toast.forbidOutAnimator();
      SmartToast.show(builder.setInAnimatorCallback(null).setDuration((int) residueDuration));
    }
  }

  @Nullable
  public static SmartToast getCurrentToast() {
    return sCurrentToast != null ? sCurrentToast.get() : null;
  }

  @NonNull
  public static <T extends SmartToast> T show(@NonNull Builder builder) {
    return new RealInterceptorChain(Collections.unmodifiableList(sInterceptors), builder)
        .proceed(builder).build().show();
  }

  public void dismiss() {
    ToastManager.getInstance().dismiss(mManagerCallback);
  }

  @NonNull
  public View getView() {
    return mToastView;
  }

  public boolean isShown() {
    return ToastManager.getInstance().isCurrent(mManagerCallback);
  }

  public boolean isShownOrQueued() {
    return ToastManager.getInstance().isCurrentOrNext(mManagerCallback);
  }

  @NonNull
  public Builder getBuilder() {
    return mBuilder.clone();
  }

  @NonNull
  public Context getContext() {
    return mToastView.getContext();
  }

  @NonNull
  public CharSequence getMessage() {
    return mBuilder.mText;
  }

  public void forbidOutAnimator() {
    mBuilder.mOutAnimatorCallback = null;
  }

  public void forbidInAnimator() {
    mBuilder.mInAnimatorCallback = null;
  }

  public long getNeedShowDuration() {
    if (mBuilder.mDuration == ToastManager.LENGTH_SHORT) {
      return ToastManager.SHORT_DURATION_MS;
    } else if (mBuilder.mDuration == ToastManager.LENGTH_LONG) {
      return ToastManager.LONG_DURATION_MS;
    } else {
      return mBuilder.mDuration;
    }
  }

  public long getShownDuration() {
    return SystemClock.elapsedRealtime() - mStartShowTime;
  }

  private <T extends SmartToast> T show() {
    if (!TextUtils.isEmpty(mBuilder.mText)) {
      WidgetUtils.runOnUIThread(
          () -> ToastManager.getInstance().show(mBuilder.mDuration, mManagerCallback));
    }
    return (T) this;
  }

  private void initView() {
    Context context = SmartWidget.getContext();
    mRootLayout = new FrameLayout(context);
    mToastView = LayoutInflater.from(context).inflate(mBuilder.mLayoutRes, mRootLayout, false);
  }

  private void showView() {
    Context context = SmartWidget.getContext();
    if (!(context instanceof Activity)) {
      Toast toast = Toast.makeText(context, mBuilder.mText, Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.show();
      onViewShown();
      return;
    }

    mStartShowTime = SystemClock.elapsedRealtime();
    sCurrentToast = new WeakReference<>(this);

    Activity activity = (Activity) context;
    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
    layoutParams.copyFrom(activity.getWindow().getAttributes());
    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
    layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
    layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    layoutParams.format = PixelFormat.TRANSLUCENT;
    layoutParams.gravity = Gravity.CENTER;
    activity.getWindowManager().addView(mRootLayout, layoutParams);

    observerViewProperty();
    mRootLayout.addView(mToastView);
    if (mBuilder.mToastBackground != null) {
      mToastView.setBackground(mBuilder.mToastBackground);
    }
    ImageView iconView = mToastView.findViewById(R.id.toast_icon);
    if (iconView != null && mBuilder.mIcon != null) {
      iconView.setImageDrawable(mBuilder.mIcon);
      iconView.setVisibility(View.VISIBLE);
    }
    TextView textView = mToastView.findViewById(R.id.toast_text);
    if (textView != null) {
      textView.setText(mBuilder.mText);
      textView.setVisibility(View.VISIBLE);
    }
    if (mBuilder.mViewAddListener != null) {
      mBuilder.mViewAddListener.onViewAdded(mToastView, mBuilder);
    }
  }

  private void observerViewProperty() {
    mToastView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            mToastView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if (mBuilder.mInAnimatorCallback != null) {
              animateViewIn();
            } else {
              onViewShown();
            }
          }
        });
    mToastView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(View v) {}

      @Override
      public void onViewDetachedFromWindow(View v) {
        if (isShownOrQueued()) {
          sHandler.post(() -> onViewHidden());
        }
      }
    });
  }

  private void dismissView() {
    sCurrentToast = null;
    if (mBuilder.mOutAnimatorCallback != null) {
      animateViewOut();
    } else {
      onViewHidden();
    }
  }

  private void animateViewIn() {
    mBuilder.mInAnimatorCallback.onStartAnimator(mToastView, new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        onViewShown();
      }
    });
  }

  private void animateViewOut() {
    mBuilder.mOutAnimatorCallback.onStartAnimator(mToastView, new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        onViewHidden();
      }
    });
  }

  private void onViewShown() {
    ToastManager.getInstance().onShown(mManagerCallback);
  }

  private void onViewHidden() {
    ToastManager.getInstance().onDismissed(mManagerCallback);
    if (mRootLayout.getParent() != null) {
      try {
        ((Activity) mRootLayout.getContext()).getWindowManager().removeViewImmediate(mRootLayout);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (mBuilder.mViewRemoveListener != null) {
      mBuilder.mViewRemoveListener.onViewRemoved(mToastView);
    }
  }

  public interface ViewAddListener {
    void onViewAdded(@NonNull View toastView, @NonNull Builder builder);
  }

  public interface ViewRemoveListener {
    void onViewRemoved(@NonNull View toastView);
  }

  public static class Builder implements Cloneable {
    protected int mLayoutRes = R.layout.toast_layout;
    protected int mDuration = ToastManager.LENGTH_LONG;
    protected CharSequence mText;
    protected Drawable mIcon;
    protected Drawable mToastBackground;
    protected Object mTag;
    protected ViewRemoveListener mViewRemoveListener;
    protected ViewAddListener mViewAddListener;
    protected PopupInterface.OnAnimatorCallback mInAnimatorCallback =
        ToastFactory.getDefaultInAnimator();
    protected PopupInterface.OnAnimatorCallback mOutAnimatorCallback =
        ToastFactory.getDefaultOutAnimator();

    @Override
    public Builder clone() {
      try {
        return (Builder) super.clone();
      } catch (Exception e) {}
      return new Builder();
    }

    public SmartToast build() {
      return new SmartToast(this);
    }

    @NonNull
    public CharSequence getText() {
      return mText;
    }

    public <T extends Builder> T setText(@StringRes int resId) {
      return setText(WidgetUtils.getString(resId));
    }

    public <T extends Builder> T setText(@NonNull CharSequence text) {
      mText = text;
      return (T) this;
    }

    public Object getTag() {
      return mTag;
    }

    public <T extends Builder> T setTag(@Nullable Object tag) {
      mTag = tag;
      return (T) this;
    }

    public int getLayoutRes() {
      return mLayoutRes;
    }

    public <T extends Builder> T setLayoutRes(@LayoutRes int layoutRes) {
      mLayoutRes = layoutRes;
      return (T) this;
    }

    public int getDuration() {
      return mDuration;
    }

    public <T extends Builder> T setDuration(@IntRange(from = -2) int duration) {
      mDuration = duration;
      return (T) this;
    }

    @Nullable
    public Drawable getIcon() {
      return mIcon;
    }

    public <T extends Builder> T setIcon(@DrawableRes int drawableId) {
      return setIcon(WidgetUtils.getDrawable(drawableId));
    }

    public <T extends Builder> T setIcon(@Nullable Drawable iconDrawable) {
      mIcon = iconDrawable;
      return (T) this;
    }

    @Nullable
    public Drawable getToastBackground() {
      return mToastBackground;
    }

    public <T extends Builder> T setToastBackground(@DrawableRes int drawableId) {
      return setToastBackground(WidgetUtils.getDrawable(drawableId));
    }

    public <T extends Builder> T setToastBackground(@Nullable Drawable toastBackground) {
      mToastBackground = toastBackground;
      return (T) this;
    }

    @Nullable
    public ViewRemoveListener getViewRemoveListener() {
      return mViewRemoveListener;
    }

    public <T extends Builder> T setViewRemoveListener(
        @Nullable ViewRemoveListener viewRemoveListener) {
      mViewRemoveListener = viewRemoveListener;
      return (T) this;
    }

    @Nullable
    public ViewAddListener getViewAddListener() {
      return mViewAddListener;
    }

    public <T extends Builder> T setViewAddListener(@Nullable ViewAddListener viewAddListener) {
      mViewAddListener = viewAddListener;
      return (T) this;
    }

    @Nullable
    public PopupInterface.OnAnimatorCallback getInAnimatorCallback() {
      return mInAnimatorCallback;
    }

    public <T extends Builder> T setInAnimatorCallback(
        @Nullable PopupInterface.OnAnimatorCallback inAnimatorCallback) {
      mInAnimatorCallback = inAnimatorCallback;
      return (T) this;
    }

    @Nullable
    public PopupInterface.OnAnimatorCallback getOutAnimatorCallback() {
      return mOutAnimatorCallback;
    }

    public <T extends Builder> T setOutAnimatorCallback(
        @Nullable PopupInterface.OnAnimatorCallback outAnimatorCallback) {
      mOutAnimatorCallback = outAnimatorCallback;
      return (T) this;
    }
  }
}
