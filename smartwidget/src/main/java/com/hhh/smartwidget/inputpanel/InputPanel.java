package com.hhh.smartwidget.inputpanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.WidgetUtils;
import com.hhh.smartwidget.popup.Popup;
import com.hhh.smartwidget.popup.PopupInterface;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class InputPanel extends Popup implements LifecycleObserver {

  public static final String AT_HALF = "@"; // 半角
  public static final String AT_FULL = "＠"; // 全角

  protected boolean mIsSent;
  protected boolean mEnableDismiss = true;
  protected boolean mIsEmotionToKeyboard;
  protected boolean mIsEmotionPanelVisible;
  protected int mKeyboardHeight;
  protected int mEmotionPanelHeight;
  protected EditText mInputView;
  protected View mEmotionPanel;
  private KeyboardVisibilityUtils.OnKeyboardVisibilityListener mKeyboardVisibilityListener;

  protected InputPanel(Builder builder) {
    super(builder);
  }

  @NonNull
  public static PopupInterface.OnAnimatorCallback getDefaultOutAnimator() {
    return (view, animatorListener) -> {
      WidgetUtils.hideSoftInput();
      ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
      ObjectAnimator translationYAnimator =
          ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), 0);
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.setDuration(280L);
      animatorSet.playTogether(alphaAnimator, translationYAnimator);
      animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
      if (animatorListener != null) {
        animatorSet.addListener(animatorListener);
      }
      animatorSet.start();
    };
  }

  @Override
  protected void onShowPopup(@Nullable Bundle bundle) {
    ((FragmentActivity) getContext()).getLifecycle().addObserver(this);
    initInputView();
    initAtView();
    initSendView();
    initEmotionView();
    initKeyboard();
  }

  @Override
  protected void onDismissPopup(@Nullable Bundle bundle) {
    ((FragmentActivity) getContext()).getLifecycle().removeObserver(this);
    KeyboardVisibilityUtils.unregisterListener(mRootLayout,
        mKeyboardVisibilityListener);
    WidgetUtils.hideSoftInput(mInputView.getWindowToken());
    Builder builder = getBuilder();
    if (!mIsSent && builder.mInputListener != null) {
      builder.mInputListener.onCanceled(this, mInputView);
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  void onPause(@NonNull LifecycleOwner owner) {
    mEnableDismiss = false;
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  void onResume(@NonNull LifecycleOwner owner) {
    if (!mIsEmotionPanelVisible && mInputView != null) {
      mInputView.post(() -> WidgetUtils.showKeyboard(mInputView));
    }
  }

  @Nullable
  public EditText getInputView() {
    return mInputView;
  }

  public void setText(@NonNull CharSequence text) {
    if (mInputView != null) {
      mInputView.setText(text);
      mInputView.setSelection(text.length());
    }
  }

  public void send() {
    Builder builder = getBuilder();
    if (builder.mInputListener != null) {
      builder.mInputListener.onSend(InputPanel.this, mInputView);
    }
    if (builder.mDismissWhenSend) {
      mIsSent = true;
      dismiss(PopupInterface.CLOSE_TYPE_POSITIVE);
    } else {
      mInputView.setText("");
    }
  }

  public boolean isEmotionPanelVisible() {
    return mIsEmotionPanelVisible;
  }

  public int getInputMinLength() {
    return getBuilder().mInputMinLength;
  }

  public int getInputMaxLength() {
    return getBuilder().mInputMaxLength;
  }

  private void initAtView() {
    View atView = findViewById(R.id.at);
    if (atView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (builder.mAtDrawable != null && atView instanceof ImageView) {
      ((ImageView) atView).setImageDrawable(builder.mAtDrawable);
    }
    if (builder.mAtCallback == null) {
      atView.setVisibility(View.GONE);
      return;
    }
    atView.setVisibility(View.VISIBLE);
    atView.setOnClickListener(v -> {
      mEnableDismiss = false;
      builder.mAtCallback.OnAt(InputPanel.this, mInputView);
    });
  }

  private void initSendView() {
    Builder builder = getBuilder();
    /**
     * 设置IME_FLAG_NO_EXTRACT_UI是为了避免在Landscape模式下，输入法自动启用全屏编辑，
     * 且可以防止 OnKeyboardVisibilityListener 失效的bug
     */
    if (builder.mImeAction > 0) {
      mInputView.setImeOptions(builder.mImeAction | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
      mInputView.setOnEditorActionListener((v, actionId, event) -> {
        if (actionId == builder.mImeAction) {
          send();
          return true;
        }
        return false;
      });
    } else {
      mInputView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }
    View sendView = findViewById(R.id.send);
    if (sendView == null) {
      return;
    }
    if (builder.mSendDrawable != null) {
      sendView.setBackground(builder.mSendDrawable);
    }
    sendView.setVisibility(View.VISIBLE);
    if (!TextUtils.isEmpty(builder.mSendText) && sendView instanceof TextView) {
      ((TextView) sendView).setText(builder.mSendText);
    }
    sendView.setOnClickListener(v -> send());
  }

  private void initEmotionView() {
    View emotionView = findViewById(R.id.emotion);
    if (emotionView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (builder.mEmotionLayout == -1) {
      emotionView.setVisibility(View.GONE);
      return;
    }
    if (builder.mEmotionDrawable != null && emotionView instanceof ImageView) {
      ((ImageView) emotionView).setImageDrawable(builder.mEmotionDrawable);
    }
    emotionView.setVisibility(View.VISIBLE);
    emotionView.setOnClickListener(v -> {
      if (mIsEmotionPanelVisible) {
        mIsEmotionToKeyboard = true;
        mIsEmotionPanelVisible = false;
        WidgetUtils.showKeyboard(mInputView);
        callbackEmotionPanelStatus(false);
      } else {
        if (mEmotionPanel == null) {
          initEmotionPanel();
        }
        mIsEmotionPanelVisible = true;
        mEnableDismiss = false;
        WidgetUtils.hideSoftInput(mInputView.getWindowToken());
        callbackEmotionPanelStatus(true);
      }
    });
  }

  private void initKeyboard() {
    Builder builder = getBuilder();
    mKeyboardVisibilityListener = new KeyboardVisibilityUtils.OnKeyboardVisibilityListener() {

      @Override
      public void onKeyboardShow(int height) {
        if (mKeyboardHeight == 0 && !builder.mShowEmotionFirst && !builder.existInAnimator()) {
          executeSwitchAnimator(mPopupView.getHeight(), -height);
        } else {
          // 点击输入框触发
          if (mIsEmotionPanelVisible) {
            mIsEmotionPanelVisible = false;
            callbackEmotionPanelStatus(false);
          }
          // 点击emotion触发
          if (mIsEmotionToKeyboard) {
            mIsEmotionToKeyboard = false;
          }
          executeSwitchAnimator(0, mEmotionPanelHeight - height);
        }
        mKeyboardHeight = height;
        if (builder.mKeyboardVisibilityListener != null) {
          builder.mKeyboardVisibilityListener.onVisibility(InputPanel.this, height, true);
        }
      }

      @Override
      public void onKeyboardHide(int height) {
        if (builder.mDismissWhenHideSoftInput) {
          if (mEnableDismiss) {
            dismiss(PopupInterface.CLOSE_TYPE_AUTO);
          } else {
            // 跳转到其他页面，比如at导致键盘收起
            mEnableDismiss = true;
            if (mIsEmotionPanelVisible) {
              executeSwitchAnimator(mEmotionPanelHeight - height, 0);
            } else {
              mPopupView.setTranslationY(mEmotionPanelHeight > 0 ? 0 : -height);
            }
          }
        } else if (!mIsEmotionPanelVisible) {
          mPopupView.setTranslationY(0);
        }
        if (builder.mKeyboardVisibilityListener != null) {
          builder.mKeyboardVisibilityListener.onVisibility(InputPanel.this, height, false);
        }
      }
    };
    KeyboardVisibilityUtils.registerListener(mRootLayout, mKeyboardVisibilityListener);
    if (!builder.mShowEmotionFirst) {
      mPopupView.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              mPopupView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
              mPopupView.setTranslationY(mPopupView.getHeight());
            }
          });
      WidgetUtils.showKeyboard(mInputView);
    } else {
      initEmotionPanel();
      callbackEmotionPanelStatus(true);
    }
  }

  private void initEmotionPanel() {
    ViewGroup emotionContainer = findViewById(R.id.emotion_container);
    // emotion 一旦初始化，就一直是 View.VISIBLE，不能改变他的可见性，否则会引起输入面板位置上的bug
    emotionContainer.setVisibility(View.VISIBLE);
    Builder builder = getBuilder();
    mEmotionPanel =
        LayoutInflater.from(getContext()).inflate(builder.mEmotionLayout, emotionContainer, true);
    if (builder.mEmotionPanelStatusListener != null) {
      builder.mEmotionPanelStatusListener.onAdded(this, mEmotionPanel);
    }
    if (builder.mEmotionHeightEqualKeyboardHeight && mKeyboardHeight > 0) {
      mEmotionPanel.getLayoutParams().height = mKeyboardHeight;
    }
    mEmotionPanel.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            mEmotionPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            mEmotionPanelHeight = mEmotionPanel.getHeight();
            if (builder.mShowEmotionFirst && !builder.existInAnimator()) {
              executeSwitchAnimator(mPopupView.getHeight(), 0);
              mIsEmotionPanelVisible = true;
            } else {
              mPopupView.setTranslationY(mEmotionPanelHeight - mKeyboardHeight);
            }
          }
        });
  }

  private void callbackEmotionPanelStatus(boolean visible) {
    Builder builder = getBuilder();
    if (builder.mEmotionPanelStatusListener != null) {
      if (visible) {
        builder.mEmotionPanelStatusListener.onShow(this, mEmotionPanel);
      } else {
        builder.mEmotionPanelStatusListener.onHide(this, mEmotionPanel);
      }
    }
  }

  private void initInputView() {
    mInputView = findViewById(R.id.input);
    Builder builder = getBuilder();
    if (!TextUtils.isEmpty(builder.mInputHint)) {
      mInputView.setHint(builder.mInputHint);
    }
    if (!TextUtils.isEmpty(builder.mInputPrefill)) {
      mInputView.setText(builder.mInputPrefill);
      mInputView.setSelection(builder.mInputPrefill.length());
    }
    mInputView.setMaxLines(builder.mMaxLines);
    if (builder.mInputType != -1) {
      mInputView.setInputType(builder.mInputType);
      if (builder.mInputType != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
          && (builder.mInputType
              & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
        mInputView.setTransformationMethod(PasswordTransformationMethod.getInstance());
      }
    }
    if (!builder.mInputFilterList.isEmpty()) {
      List<InputFilter> inputFilterList = new ArrayList<>();
      if (mInputView.getFilters().length > 0) {
        Collections.addAll(inputFilterList, mInputView.getFilters());
      }
      inputFilterList.addAll(builder.mInputFilterList);
      mInputView.setFilters(inputFilterList.toArray(new InputFilter[inputFilterList.size()]));
    }
    invalidatePositiveViewForInput(mInputView.getText());
    mInputView.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

      @Override
      public void afterTextChanged(Editable editable) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        invalidatePositiveViewForInput(s);
        if (builder.mAlwaysCallInputListener) {
          builder.mInputListener.onInput(InputPanel.this, mInputView);
        }
        if (builder.mEnableAtCharForAt && s.length() > 0 && count == 1) {
          String text = s.toString().substring(start, start + 1);
          if (AT_HALF.equals(text) || AT_FULL.equals(text)) {
            builder.mAtCallback.OnAt(InputPanel.this, mInputView);
          }
        }
      }
    });
  }

  private void invalidatePositiveViewForInput(CharSequence text) {
    View sendView = findViewById(R.id.send);
    if (sendView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (builder.mInputMinLength > 0) {
      if (TextUtils.isEmpty(text) || text.length() < builder.mInputMinLength) {
        sendView.setEnabled(false);
        return;
      }
    }
    if (builder.mInputMaxLength > 0) {
      if (!TextUtils.isEmpty(text) && text.length() > builder.mInputMaxLength) {
        sendView.setEnabled(false);
        return;
      }
    }
    sendView.setEnabled(true);
  }

  private void executeSwitchAnimator(int startTranslationY, int endTranslationY) {
    ObjectAnimator animator =
        ObjectAnimator.ofFloat(mPopupView, View.TRANSLATION_Y, startTranslationY, endTranslationY);
    animator.setInterpolator(new DecelerateInterpolator(1.5f));
    animator.setDuration(280L);
    animator.start();
  }

  protected Builder getBuilder() {
    return (Builder) mBuilder;
  }

  public static class Builder extends Popup.Builder {

    protected final List<InputFilter> mInputFilterList = new ArrayList<>();
    protected int mInputMinLength = 1;
    protected int mInputMaxLength;
    protected int mMaxLines = 1;
    protected int mInputType = -1;
    protected int mImeAction;
    protected int mEmotionLayout = -1;
    protected boolean mShowEmotionFirst;
    protected boolean mEnableAtCharForAt;
    protected boolean mAlwaysCallInputListener;
    protected boolean mDismissWhenHideSoftInput = true;
    protected boolean mDismissWhenSend = true;
    protected boolean mEmotionHeightEqualKeyboardHeight;
    protected CharSequence mSendText;
    protected CharSequence mInputHint;
    protected CharSequence mInputPrefill;
    protected Drawable mAtDrawable;
    protected Drawable mEmotionDrawable;
    protected Drawable mSendDrawable;

    protected InputPanelInterface.OnAtCallback mAtCallback;
    protected InputPanelInterface.OnInputListener mInputListener;
    protected InputPanelInterface.OnEmotionPanelStatusListener mEmotionPanelStatusListener;
    protected InputPanelInterface.OnKeyboardVisibilityListener mKeyboardVisibilityListener;

    public Builder(@NonNull FragmentActivity activity) {
      super(activity);
      mPopupType = PopupInterface.POPUP_TYPE_INPUT;
      mExcluded = PopupInterface.Excluded.NOT_AGAINST;
      mOutAnimatorCallback = getDefaultOutAnimator();
    }

    @Override
    public InputPanel build() {
      return new InputPanel(this);
    }

    public boolean existInAnimator() {
      return mInAnimatorCallback != null;
    }

    public <T extends Builder> T setAtCallback(
        @Nullable InputPanelInterface.OnAtCallback atCallback) {
      mAtCallback = atCallback;
      return (T) this;
    }

    public <T extends Builder> T setInputListener(
        @NonNull InputPanelInterface.OnInputListener inputListener) {
      mInputListener = inputListener;
      return (T) this;
    }

    public <T extends Builder> T setEmotionPanelStatusListener(
        @Nullable InputPanelInterface.OnEmotionPanelStatusListener emotionPanelStatusListener) {
      mEmotionPanelStatusListener = emotionPanelStatusListener;
      return (T) this;
    }

    public <T extends Builder> T setKeyboardVisibilityListener(
        @Nullable InputPanelInterface.OnKeyboardVisibilityListener keyboardVisibilityListener) {
      mKeyboardVisibilityListener = keyboardVisibilityListener;
      return (T) this;
    }

    public <T extends Builder> T setSendText(@StringRes int sendText) {
      return setSendText(mActivity.getString(sendText));
    }

    public <T extends Builder> T setSendText(@NonNull CharSequence sendText) {
      mSendText = sendText;
      return (T) this;
    }

    public <T extends Builder> T setInputHint(@StringRes int inputHint) {
      return setInputHint(mActivity.getString(inputHint));
    }

    public <T extends Builder> T setInputHint(CharSequence inputHint) {
      mInputHint = inputHint;
      return (T) this;
    }

    public <T extends Builder> T setInputPrefill(@StringRes int inputPrefill) {
      return setInputPrefill(mActivity.getString(inputPrefill));
    }

    public <T extends Builder> T setInputPrefill(CharSequence inputPrefill) {
      mInputPrefill = inputPrefill;
      return (T) this;
    }

    public <T extends Builder> T setInputMinLength(
        @IntRange(from = 0L, to = Integer.MAX_VALUE) int inputMinLength) {
      mInputMinLength = inputMinLength;
      return (T) this;
    }

    public <T extends Builder> T setInputMaxLength(
        @IntRange(from = 0L, to = Integer.MAX_VALUE) int inputMaxLength) {
      mInputMaxLength = inputMaxLength;
      return (T) this;
    }

    public <T extends Builder> T setMaxLines(@IntRange(from = 1L) int maxLines) {
      mMaxLines = maxLines;
      return (T) this;
    }

    public <T extends Builder> T setInputType(int inputType) {
      mInputType = inputType;
      return (T) this;
    }

    public <T extends Builder> T setImeAction(int imeAction) {
      mImeAction = imeAction;
      return (T) this;
    }

    public <T extends Builder> T setEmotionLayout(@LayoutRes int emotionLayout) {
      mEmotionLayout = emotionLayout;
      return (T) this;
    }

    public <T extends Builder> T setShowEmotionFirst(boolean showEmotionFirst) {
      mShowEmotionFirst = showEmotionFirst;
      return (T) this;
    }

    public <T extends Builder> T setEnableAtCharForAt(boolean enableAtCharForAt) {
      mEnableAtCharForAt = enableAtCharForAt;
      return (T) this;
    }

    public <T extends Builder> T setAlwaysCallInputListener(boolean alwaysCallInputListener) {
      mAlwaysCallInputListener = alwaysCallInputListener;
      return (T) this;
    }

    public <T extends Builder> T setDismissWhenHideSoftInput(boolean dismissWhenHideSoftInput) {
      mDismissWhenHideSoftInput = dismissWhenHideSoftInput;
      return (T) this;
    }

    public <T extends Builder> T setDismissWhenSend(boolean dismissWhenSend) {
      mDismissWhenSend = dismissWhenSend;
      return (T) this;
    }

    public <T extends Builder> T setEmotionHeightEqualKeyboardHeight(
        boolean emotionHeightEqualKeyboardHeight) {
      mEmotionHeightEqualKeyboardHeight = emotionHeightEqualKeyboardHeight;
      return (T) this;
    }

    public <T extends Builder> T setAtDrawable(@DrawableRes int atDrawable) {
      return setAtDrawable(WidgetUtils.getDrawable(atDrawable));
    }

    public <T extends Builder> T setAtDrawable(@NonNull Drawable atDrawable) {
      mAtDrawable = atDrawable;
      return (T) this;
    }

    public <T extends Builder> T setEmotionDrawable(@DrawableRes int emotionDrawable) {
      return setEmotionDrawable(WidgetUtils.getDrawable(emotionDrawable));
    }

    public <T extends Builder> T setEmotionDrawable(@NonNull Drawable emotionDrawable) {
      mEmotionDrawable = emotionDrawable;
      return (T) this;
    }

    public <T extends Builder> T setSendDrawable(@DrawableRes int sendDrawable) {
      return setSendDrawable(WidgetUtils.getDrawable(sendDrawable));
    }

    public <T extends Builder> T setSendDrawable(@NonNull Drawable sendDrawable) {
      mSendDrawable = sendDrawable;
      return (T) this;
    }

    public <T extends Builder> T addInputFilter(@Nullable InputFilter inputFilter) {
      if (inputFilter != null) {
        mInputFilterList.add(inputFilter);
      }
      return (T) this;
    }

    public <T extends Builder> T addInputFilterList(@NonNull List<InputFilter> inputFilterList) {
      mInputFilterList.addAll(inputFilterList);
      return (T) this;
    }
  }
}
