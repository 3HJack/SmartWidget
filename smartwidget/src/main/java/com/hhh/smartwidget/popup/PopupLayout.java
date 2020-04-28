package com.hhh.smartwidget.popup;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import com.hhh.smartwidget.R;

public class PopupLayout extends LinearLayout {

  private int mDelayMeasureViewId;
  private int mMaxHeight = Integer.MAX_VALUE;
  private int mMaxWidth = Integer.MAX_VALUE;

  public PopupLayout(@NonNull Context context) {
    super(context);
    getDelayMeasureViewIds(context, null, 0);
  }

  public PopupLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    getDelayMeasureViewIds(context, attrs, 0);
  }

  public PopupLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    getDelayMeasureViewIds(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PopupLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    getDelayMeasureViewIds(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    if (widthSize > mMaxWidth) {
      widthSize = mMaxWidth;
    }
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    if (heightSize > mMaxHeight) {
      heightSize = mMaxHeight;
    }
    widthMeasureSpec =
        MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.getMode(widthMeasureSpec));
    heightMeasureSpec =
        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.getMode(heightMeasureSpec));
    if (mDelayMeasureViewId == 0) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    } else if (getOrientation() == HORIZONTAL) {
      measureHorizontal(widthMeasureSpec, heightMeasureSpec);
    } else {
      measureVertical(widthMeasureSpec, heightMeasureSpec);
    }
  }

  public PopupLayout setMaxHeight(@Px int maxHeight) {
    mMaxHeight = maxHeight;
    return this;
  }

  public PopupLayout setMaxWidth(@Px int maxWidth) {
    mMaxWidth = maxWidth;
    return this;
  }

  private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int availableWidth = widthSize - getPaddingLeft() - getPaddingRight();

    View delayMeasureView = null;
    MarginLayoutParams layoutParams;
    int childCount = getChildCount();
    for (int i = 0; i < childCount; ++i) {
      View view = getChildAt(i);
      if (view.getId() == mDelayMeasureViewId) {
        delayMeasureView = view;
      } else if (view.getVisibility() != GONE) {
        measureChildWithMargins(view, MeasureSpec.makeMeasureSpec(availableWidth, widthMode), 0,
            heightMeasureSpec, 0);
        layoutParams = (MarginLayoutParams) view.getLayoutParams();
        availableWidth -=
            view.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
      }
    }
    if (delayMeasureView == null) {
      throw new RuntimeException("PopupLayout_delay_measure_id is invalid!!!");
    }
    if (delayMeasureView.getVisibility() != GONE) {
      measureChildWithMargins(delayMeasureView,
          MeasureSpec.makeMeasureSpec(availableWidth, widthMode), 0, heightMeasureSpec, 0);
      layoutParams = (MarginLayoutParams) delayMeasureView.getLayoutParams();
      availableWidth -=
          delayMeasureView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
    }
    setMeasuredDimension(MeasureSpec.makeMeasureSpec(widthSize - availableWidth, widthMode),
        heightMeasureSpec);
  }

  private void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    int availableHeight = heightSize - getPaddingTop() - getPaddingBottom();

    View delayMeasureView = null;
    MarginLayoutParams layoutParams;
    int childCount = getChildCount();
    for (int i = 0; i < childCount; ++i) {
      View view = getChildAt(i);
      if (view.getId() == mDelayMeasureViewId) {
        delayMeasureView = view;
      } else if (view.getVisibility() != GONE) {
        measureChildWithMargins(view, widthMeasureSpec, 0,
            MeasureSpec.makeMeasureSpec(availableHeight, heightMode), 0);
        layoutParams = (MarginLayoutParams) view.getLayoutParams();
        availableHeight -=
            view.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
      }
    }
    if (delayMeasureView == null) {
      throw new RuntimeException("PopupLayout_delay_measure_id is invalid!!!");
    }
    if (delayMeasureView.getVisibility() != GONE) {
      measureChildWithMargins(delayMeasureView, widthMeasureSpec, 0,
          MeasureSpec.makeMeasureSpec(availableHeight, heightMode), 0);
      layoutParams = (MarginLayoutParams) delayMeasureView.getLayoutParams();
      availableHeight -=
          delayMeasureView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
    }
    setMeasuredDimension(widthMeasureSpec,
        MeasureSpec.makeMeasureSpec(heightSize - availableHeight, heightMode));
  }

  private void getDelayMeasureViewIds(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray array =
        context.obtainStyledAttributes(attrs, R.styleable.PopupLayout, defStyleAttr, 0);
    mDelayMeasureViewId = array.getResourceId(R.styleable.PopupLayout_delay_measure_id, 0);
    mMaxHeight =
        array.getDimensionPixelSize(R.styleable.PopupLayout_android_maxHeight, Integer.MAX_VALUE);
    mMaxWidth =
        array.getDimensionPixelSize(R.styleable.PopupLayout_android_maxWidth, Integer.MAX_VALUE);
    array.recycle();
  }
}
