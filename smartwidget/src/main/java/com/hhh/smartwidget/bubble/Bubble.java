package com.hhh.smartwidget.bubble;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.WidgetUtils;
import com.hhh.smartwidget.popup.Popup;
import com.hhh.smartwidget.popup.PopupInterface;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Bubble extends Popup {

  protected Bubble(Builder builder) {
    super(builder);
  }

  @Override
  protected void onShowPopup(@Nullable Bundle bundle) {
    initSimpleView();
    initRecyclerView();
    Builder builder = getBuilder();
    if (ViewCompat.isLaidOut(builder.mAnchorView)) {
      startSetPosition();
    } else {
      builder.mAnchorView.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              builder.mAnchorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
              startSetPosition();
            }
          });
    }
  }

  private Builder getBuilder() {
    return (Builder) mBuilder;
  }

  private void initSimpleView() {
    Builder builder = getBuilder();
    TextView textView = findViewById(R.id.text);
    if (textView != null) {
      textView.setText(builder.mText);
    }
    if (builder.mButtonCallback != null) {
      mPopupView.setOnClickListener(v -> {
        if (builder.mAutoDismiss) {
          dismiss(PopupInterface.CLOSE_TYPE_POSITIVE);
        }
        builder.mButtonCallback.onClick(Bubble.this, v);
      });
    }
  }

  private void initRecyclerView() {
    RecyclerView recyclerView = findViewById(R.id.recycler_view);
    if (recyclerView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (builder.mLayoutManager != null) {
      recyclerView.setLayoutManager(builder.mLayoutManager);
    } else {
      recyclerView.setLayoutManager(builder.mLayoutManager = new LinearLayoutManager(getContext()));
    }
    recyclerView.setAdapter(builder.mAdapter);
  }

  private void startSetPosition() {
    if (ViewCompat.isLaidOut(mPopupView)) {
      setPosition();
    } else {
      mPopupView.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              if (ViewCompat.isLaidOut(mPopupView)) {
                mPopupView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setPosition();
              }
            }
          });
    }
  }

  private void setPosition() {
    View arrowView = findViewById(R.id.arrow);
    Builder builder = getBuilder();
    int[] location = new int[2];
    builder.mAnchorView.getLocationInWindow(location);
    int anchorViewWidth = builder.mAnchorView.getWidth();
    int anchorViewHeight = builder.mAnchorView.getHeight();
    int popupViewWidth = mPopupView.getWidth();
    int popupViewHeight = mPopupView.getHeight();
    int maxTranslationY =
        WidgetUtils.getWindowHeight(getContext()) - popupViewHeight - builder.mVerticalMargin;
    int maxTranslationX =
        WidgetUtils.getWindowWidth(getContext()) - popupViewWidth - builder.mHorizontalMargin;
    int paddingTop = mRootLayout.getPaddingTop();
    int translationMidX;
    int translationMidY;
    int translationX;
    int translationY;
    switch (builder.mPosition) {
      case LEFT:
      case RIGHT:
        if (builder.mPosition == BubbleInterface.Position.LEFT) {
          translationX = location[0] - popupViewWidth;
        } else {
          translationX = location[0] + anchorViewWidth;
        }
        translationMidY = ((anchorViewHeight - popupViewHeight) >> 1) + location[1] - paddingTop;
        translationY =
            Math.min(Math.max(translationMidY, builder.mVerticalMargin), maxTranslationY);
        mPopupView.setTranslationX(translationX);
        mPopupView.setTranslationY(translationY);
        if (arrowView != null && translationY != translationMidY) {
          arrowView.setTranslationY(translationMidY - translationY + builder.mArrowOffset);
        }
        break;
      case TOP:
      case BOTTOM:
        if (builder.mPosition == BubbleInterface.Position.TOP) {
          translationY = location[1] - popupViewHeight - paddingTop;
        } else {
          translationY = location[1] + anchorViewHeight - paddingTop;
        }
        translationMidX = ((anchorViewWidth - popupViewWidth) >> 1) + location[0];
        translationX =
            Math.min(Math.max(translationMidX, builder.mHorizontalMargin), maxTranslationX);
        mPopupView.setTranslationX(translationX);
        mPopupView.setTranslationY(translationY);
        if (arrowView != null && translationX != translationMidX) {
          arrowView.setTranslationX(translationMidX - translationX + builder.mArrowOffset);
        }
        break;
      default:
        break;
    }
  }

  public static class Builder extends Popup.Builder {

    protected Bubble mBubble;
    protected View mAnchorView;
    protected CharSequence mText;
    protected BubbleInterface.Position mPosition;
    protected BubbleInterface.ButtonCallback mButtonCallback;
    protected int mListItemLayout;
    protected List<BubbleInterface.BubbleItem> mBubbleItems;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected BubbleInterface.ListCallback mListCallback;
    protected boolean mAutoDismiss = true;
    protected int mHorizontalMargin;
    protected int mVerticalMargin;
    protected int mArrowOffset;

    public Builder(@NonNull Activity activity) {
      super(activity);
      mPopupType = PopupInterface.POPUP_TYPE_BUBBLE;
      mExcluded = PopupInterface.Excluded.SAME_TYPE;
      mInAnimatorCallback = BubbleFactory.getDefaultInAnimator();
      mOutAnimatorCallback = BubbleFactory.getDefaultOutAnimator();
      mPosition = BubbleInterface.Position.TOP;
      mHorizontalMargin = WidgetUtils.dip2px(15f);
    }

    @Override
    public Bubble build() {
      mBubble = new Bubble(this);
      return mBubble;
    }

    /**
     * 锚点 View
     */
    public <T extends Builder> T setAnchorView(@NonNull View anchorView) {
      mAnchorView = anchorView;
      return (T) this;
    }

    /**
     * 展示的文本
     */
    public <T extends Builder> T setText(@NonNull CharSequence text) {
      mText = text;
      return (T) this;
    }

    /**
     * 相对锚点的位置
     */
    public <T extends Builder> T setPosition(@NonNull BubbleInterface.Position position) {
      mPosition = position;
      return (T) this;
    }

    /**
     * Bubble点击回调
     */
    public <T extends Builder> T setButtonCallback(
        @Nullable BubbleInterface.ButtonCallback buttonCallback) {
      mButtonCallback = buttonCallback;
      return (T) this;
    }

    /**
     * 默认值：true
     * 表示点击泡泡后是否自动dismiss
     */
    public <T extends Builder> T setAutoDismiss(boolean autoDismiss) {
      mAutoDismiss = autoDismiss;
      return (T) this;
    }

    /**
     * {@link #mPosition}为{@link BubbleInterface.Position#TOP}或者{@link BubbleInterface.Position#BOTTOM}
     * 时，距离屏幕左右边缘的最小间隔
     */
    public <T extends Builder> T setHorizontalMargin(@Px int horizontalMargin) {
      mHorizontalMargin = horizontalMargin;
      return (T) this;
    }

    /**
     * {@link #mPosition}为{@link BubbleInterface.Position#LEFT}或者{@link BubbleInterface.Position#RIGHT}
     * 时，距离屏幕上下边缘的最小间隔
     */
    public <T extends Builder> T setVerticalMargin(@Px int verticalMargin) {
      mVerticalMargin = verticalMargin;
      return (T) this;
    }

    /**
     * 箭头的偏移，开发者可根据需要自行设置
     * {@link #mPosition}为{@link BubbleInterface.Position#TOP}或者{@link BubbleInterface.Position#BOTTOM}时，为横坐标偏移
     * {@link #mPosition}为{@link BubbleInterface.Position#LEFT}或者{@link BubbleInterface.Position#RIGHT}时，为纵坐标偏移
     */
    public <T extends Builder> T setArrowOffset(@Px int arrowOffset) {
      mArrowOffset = arrowOffset;
      return (T) this;
    }

    public <T extends Builder> T setAdapter(@NonNull RecyclerView.Adapter adapter) {
      mAdapter = adapter;
      return (T) this;
    }

    public <T extends Builder> T setLayoutManager(
        @Nullable RecyclerView.LayoutManager layoutManager) {
      mLayoutManager = layoutManager;
      return (T) this;
    }

    public int getListItemLayout() {
      return mListItemLayout;
    }

    public <T extends Builder> T setListItemLayout(@LayoutRes int listItemLayout) {
      mListItemLayout = listItemLayout;
      return (T) this;
    }

    public List<BubbleInterface.BubbleItem> getBubbleItems() {
      return mBubbleItems;
    }

    public <T extends Builder> T setBubbleItems(
        @NonNull List<BubbleInterface.BubbleItem> bubbleItems) {
      mBubbleItems = bubbleItems;
      return (T) this;
    }

    public BubbleInterface.ListCallback getListCallback() {
      return mListCallback;
    }

    public <T extends Builder> T setListCallback(
        @Nullable BubbleInterface.ListCallback listCallback) {
      mListCallback = listCallback;
      return (T) this;
    }

    public Bubble getBubble() {
      return mBubble;
    }
  }
}
