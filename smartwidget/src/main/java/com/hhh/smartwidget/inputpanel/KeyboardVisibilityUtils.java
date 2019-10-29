package com.hhh.smartwidget.inputpanel;

import java.util.WeakHashMap;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

public final class KeyboardVisibilityUtils {

  private static final WeakHashMap<OnKeyboardVisibilityListener, ViewTreeObserver.OnGlobalLayoutListener> LISTENER_MAP =
      new WeakHashMap<>();

  private KeyboardVisibilityUtils() {}

  public static void registerListener(@NonNull View view,
      @NonNull OnKeyboardVisibilityListener listener) {
    if (LISTENER_MAP.get(listener) != null) {
      return;
    }
    ViewTreeObserver.OnGlobalLayoutListener layoutListener =
        new ViewTreeObserver.OnGlobalLayoutListener() {

          private int viewVisibleHeight;

          @Override
          public void onGlobalLayout() {
            // 获取当前根视图在屏幕上显示的大小
            Rect rect = new Rect();
            view.getWindowVisibleDisplayFrame(rect);
            int visibleHeight = rect.height();
            if (viewVisibleHeight == 0) {
              viewVisibleHeight = visibleHeight;
              return;
            }
            // 根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
            if (viewVisibleHeight == visibleHeight) {
              return;
            }
            int minHeightDiff = view.getHeight() / 4;
            // 根视图显示高度变小超过 minHeightDiff，可以看作软键盘显示了
            if (viewVisibleHeight - visibleHeight > minHeightDiff) {
              listener.onKeyboardShow(viewVisibleHeight - visibleHeight);
              viewVisibleHeight = visibleHeight;
              return;
            }
            // 根视图显示高度变大超过 minHeightDiff，可以看作软键盘隐藏了
            if (visibleHeight - viewVisibleHeight > minHeightDiff) {
              listener.onKeyboardHide(visibleHeight - viewVisibleHeight);
              viewVisibleHeight = visibleHeight;
              return;
            }
          }
        };
    view.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    LISTENER_MAP.put(listener, layoutListener);
  }

  public static void unregisterListener(@NonNull View view,
      @NonNull OnKeyboardVisibilityListener listener) {
    view.getViewTreeObserver().removeOnGlobalLayoutListener(LISTENER_MAP.remove(listener));
  }

  public interface OnKeyboardVisibilityListener {
    void onKeyboardShow(int height);

    void onKeyboardHide(int height);
  }
}
