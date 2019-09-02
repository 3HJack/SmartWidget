package com.hhh.smartwidget.bubble;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class BubbleInterface {

  private BubbleInterface() {}

  public enum Position {
    LEFT, TOP, RIGHT, BOTTOM
  }

  public interface ButtonCallback {
    void onClick(@NonNull Bubble bubble, @NonNull View view);
  }

  public interface ListCallback {
    void onSelection(@NonNull Bubble bubble, @Nullable View itemView,
        @IntRange(from = 0) int position);
  }

  public static class BubbleItem {
    public CharSequence mText;
    public Drawable mIcon;
  }
}
