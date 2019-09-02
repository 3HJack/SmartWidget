package com.hhh.smartwidget.popup;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.hhh.smartwidget.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

public class PopupRootLayout extends FrameLayout {

  private int mChildMaxHeight = Integer.MAX_VALUE;
  private int mChildMaxWidth = Integer.MAX_VALUE;

  public PopupRootLayout(@NonNull Context context) {
    super(context);
    initViewProperty(context, null, 0);
  }

  public PopupRootLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initViewProperty(context, attrs, 0);
  }

  public PopupRootLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initViewProperty(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PopupRootLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initViewProperty(context, attrs, defStyleAttr);
  }

  @Override
  protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
      int parentHeightMeasureSpec, int heightUsed) {
    int width = MeasureSpec.getSize(parentWidthMeasureSpec);
    int height = MeasureSpec.getSize(parentHeightMeasureSpec);
    if (width > mChildMaxWidth) {
      widthUsed = width - mChildMaxWidth;
    }
    if (height > mChildMaxHeight) {
      heightUsed = height - mChildMaxHeight;
    }
    super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec,
        heightUsed);
  }

  public PopupRootLayout setChildMaxHeight(@Px int childMaxHeight) {
    mChildMaxHeight = childMaxHeight;
    return this;
  }

  public PopupRootLayout setChildMaxWidth(@Px int childMaxWidth) {
    mChildMaxWidth = childMaxWidth;
    return this;
  }

  private void initViewProperty(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray array =
        context.obtainStyledAttributes(attrs, R.styleable.PopupRootLayout, defStyleAttr, 0);
    mChildMaxHeight =
        array.getDimensionPixelSize(R.styleable.PopupRootLayout_maxChildHeight, Integer.MAX_VALUE);
    mChildMaxWidth =
        array.getDimensionPixelSize(R.styleable.PopupRootLayout_maxChildWidth, Integer.MAX_VALUE);
    array.recycle();
  }
}
