package com.hhh.smartwidget.inputpanel;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

public final class InputPanelInterface {

  private InputPanelInterface() {}

  public interface OnAtCallback {
    /**
     * 触发at动作的回调
     */
    void OnAt(@NonNull InputPanel inputPanel, @NonNull EditText inputView);
  }

  public interface OnEmotionPanelStatusListener {
    /**
     * 第一次触发emotion的展示
     */
    default void onAdded(@NonNull InputPanel inputPanel, @NonNull View emotionPanelView) {}

    /**
     * 展示emotion
     */
    default void onShow(@NonNull InputPanel inputPanel, @NonNull View emotionPanelView) {}

    /**
     * 隐藏emotion
     */
    default void onHide(@NonNull InputPanel inputPanel, @NonNull View emotionPanelView) {}
  }

  public interface OnInputListener {
    /**
     * 如果{@link InputPanel.Builder#mAlwaysCallInputListener} 为 true，则文本一旦发生变化就会回调
     */
    default void onInput(@NonNull InputPanel inputPanel, @NonNull EditText inputView) {}

    /**
     * 取消输入
     */
    default void onCanceled(@NonNull InputPanel inputPanel, @NonNull EditText inputView) {}

    /**
     * 发送输入
     */
    default void onSend(@NonNull InputPanel inputPanel, @NonNull EditText inputView) {}
  }

  public interface OnKeyboardVisibilityListener {
    /**
     * @param inputPanel
     * @param height 键盘高度
     * @param visible true 表示键盘显示，false表示键盘隐藏
     */
    void onVisibility(@NonNull InputPanel inputPanel, int height, boolean visible);
  }
}
