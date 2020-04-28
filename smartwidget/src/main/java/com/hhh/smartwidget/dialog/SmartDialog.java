package com.hhh.smartwidget.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.WidgetUtils;
import com.hhh.smartwidget.dialog.adjust.AdjustStyle;
import com.hhh.smartwidget.inputpanel.KeyboardVisibilityUtils;
import com.hhh.smartwidget.popup.Popup;
import com.hhh.smartwidget.popup.PopupInterface;

public class SmartDialog extends Popup implements View.OnClickListener {

  protected EditText mInputView;
  private KeyboardVisibilityUtils.OnKeyboardVisibilityListener mKeyboardVisibilityListener;

  protected SmartDialog(Builder builder) {
    super(builder);
  }

  @Override
  protected void onShowPopup(@Nullable Bundle bundle) {
    initTitleView();
    initContentView();
    initDetailView();
    initButton();
    initIconView();
    initInputView();
    initRecyclerView();
    for (AdjustStyle adjustStyle : getBuilder().mAdjustStyles) {
      adjustStyle.apply(this);
    }
  }

  @Override
  protected void onDismissPopup(@Nullable Bundle bundle) {
    if (mInputView != null) {
      KeyboardVisibilityUtils.unregisterListener(mRootLayout,
          mKeyboardVisibilityListener);
      WidgetUtils.hideSoftInput(mInputView.getWindowToken());
    }
  }

  @Override
  public final void onClick(View v) {
    Builder builder = (Builder) mBuilder;
    int id = v.getId();
    if (id == R.id.positive) {
      if (builder.mPositiveCallback != null) {
        builder.mPositiveCallback.onClick(this, v);
      }
      if (!builder.mAlwaysCallSingleChoiceCallback) {
        sendSingleChoiceCallback(null);
      }
      if (!builder.mAlwaysCallMultiChoiceCallback) {
        sendMultiChoiceCallback();
      }
      if (!builder.mAlwaysCallInputCallback) {
        sendInputCallback();
      }
      if (builder.mAutoDismiss) {
        dismiss(PopupInterface.CLOSE_TYPE_POSITIVE);
      }
    } else if (id == R.id.negative) {
      if (builder.mNegativeCallback != null) {
        builder.mNegativeCallback.onClick(this, v);
      }
      if (builder.mAutoDismiss) {
        cancelPopup(PopupInterface.CLOSE_TYPE_NEGATIVE);
      }
    } else if (id == R.id.close) {
      if (builder.mCloseCallback != null) {
        builder.mCloseCallback.onClick(this, v);
      }
      if (builder.mAutoDismiss) {
        cancelPopup(PopupInterface.CLOSE_TYPE_NEGATIVE);
      }
    }
  }

  @NonNull
  public Builder getBuilder() {
    return (Builder) mBuilder;
  }

  private void sendSingleChoiceCallback(@Nullable View itemView) {
    Builder builder = getBuilder();
    if (builder.mListCallback == null) {
      return;
    }
    builder.mListCallback.onSelection(this, itemView, builder.mSelectedIndex);
  }

  private void sendMultiChoiceCallback() {
    Builder builder = getBuilder();
    if (builder.mListCallbackMultiChoice == null) {
      return;
    }
    Collections.sort(builder.mSelectedIndices);
    builder.mListCallbackMultiChoice.onSelection(this, builder.mSelectedIndices);
  }

  private void sendInputCallback() {
    Builder builder = getBuilder();
    if (builder.mInputCallback == null || mInputView == null) {
      return;
    }
    builder.mInputCallback.onInput(this, mInputView, mInputView.getText());
  }

  private void initTitleView() {
    TextView titleView = findViewById(R.id.title);
    if (titleView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (!TextUtils.isEmpty(builder.mTitleText)) {
      titleView.setText(builder.mTitleText);
      titleView.setVisibility(View.VISIBLE);
    } else {
      titleView.setVisibility(TextUtils.isEmpty(titleView.getText()) ? View.GONE : View.VISIBLE);
    }
  }

  private void initContentView() {
    TextView contentView = findViewById(R.id.content);
    if (contentView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (!TextUtils.isEmpty(builder.mContentText)) {
      contentView.setText(builder.mContentText);
      contentView.setVisibility(View.VISIBLE);
    } else {
      contentView
          .setVisibility(TextUtils.isEmpty(contentView.getText()) ? View.GONE : View.VISIBLE);
    }
  }

  private void initDetailView() {
    TextView detailView = findViewById(R.id.detail);
    if (detailView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (!TextUtils.isEmpty(builder.mDetailText)) {
      detailView.setText(builder.mDetailText);
      detailView.setVisibility(View.VISIBLE);
    } else {
      detailView.setVisibility(TextUtils.isEmpty(detailView.getText()) ? View.GONE : View.VISIBLE);
    }
  }

  private void initButton() {
    Builder builder = getBuilder();
    TextView positiveView = findViewById(R.id.positive);
    if (positiveView != null) {
      if (!TextUtils.isEmpty(builder.mPositiveText)) {
        positiveView.setText(builder.mPositiveText);
        positiveView.setVisibility(View.VISIBLE);
      } else {
        positiveView
            .setVisibility(TextUtils.isEmpty(positiveView.getText()) ? View.GONE : View.VISIBLE);
      }
      if (positiveView.getVisibility() == View.VISIBLE) {
        positiveView.setOnClickListener(this);
      }
    }
    TextView negativeView = findViewById(R.id.negative);
    if (negativeView != null) {
      if (!TextUtils.isEmpty(builder.mNegativeText)) {
        negativeView.setText(builder.mNegativeText);
        negativeView.setVisibility(View.VISIBLE);
      } else {
        negativeView
            .setVisibility(TextUtils.isEmpty(negativeView.getText()) ? View.GONE : View.VISIBLE);
      }
      if (negativeView.getVisibility() == View.VISIBLE) {
        negativeView.setOnClickListener(this);
      }
    }
    View closeView = findViewById(R.id.close);
    if (closeView != null) {
      closeView.setOnClickListener(this);
    }
  }

  private void initIconView() {
    ImageView iconView = findViewById(R.id.icon);
    if (iconView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (builder.mIcon != null) {
      iconView.setImageDrawable(builder.mIcon);
      iconView.setVisibility(View.VISIBLE);
    } else if (builder.mIconUri != null) {
      iconView.setImageURI(builder.mIconUri);
      iconView.setVisibility(View.VISIBLE);
    } else {
      iconView.setVisibility(iconView.getDrawable() == null ? View.GONE : View.VISIBLE);
    }
  }

  private void initInputView() {
    mInputView = findViewById(R.id.input);
    if (mInputView == null) {
      return;
    }
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
    if (builder.mInputMinLength > 0 || builder.mInputMaxLength > 0) {
      invalidatePositiveViewForInput(mInputView.getText());
    }
    mInputView.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

      @Override
      public void afterTextChanged(Editable editable) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        invalidatePositiveViewForInput(s);
        if (builder.mAlwaysCallInputCallback) {
          builder.mInputCallback.onInput(SmartDialog.this, mInputView, s);
        }
      }
    });
    mKeyboardVisibilityListener = new KeyboardVisibilityUtils.OnKeyboardVisibilityListener() {
      @Override
      public void onKeyboardShow(int height) {
        mPopupView.setTranslationY(-(height >> 1));
      }

      @Override
      public void onKeyboardHide(int height) {
        mPopupView.setTranslationY(0);
      }
    };
    KeyboardVisibilityUtils.registerListener(mRootLayout, mKeyboardVisibilityListener);
    WidgetUtils.showKeyboard(mInputView);
  }

  private void invalidatePositiveViewForInput(CharSequence text) {
    TextView positiveView = findViewById(R.id.positive);
    if (positiveView == null) {
      return;
    }
    Builder builder = getBuilder();
    if (TextUtils.isEmpty(text) && !builder.mInputAllowEmpty) {
      positiveView.setEnabled(false);
      return;
    }
    if (builder.mInputMinLength > 0) {
      if (TextUtils.isEmpty(text) || text.length() < builder.mInputMinLength) {
        positiveView.setEnabled(false);
        return;
      }
    }
    if (builder.mInputMaxLength > 0) {
      if (!TextUtils.isEmpty(text) && text.length() > builder.mInputMaxLength) {
        positiveView.setEnabled(false);
        return;
      }
    }
    positiveView.setEnabled(true);
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
      recyclerView
          .setLayoutManager(builder.mLayoutManager = new LinearLayoutManager(getContext()));
    }
    Collections.sort(builder.mSelectedIndices);
    recyclerView.setAdapter(builder.mAdapter);
    recyclerView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            int selectedIndex = -1;
            if (builder.mSelectedIndex > -1) {
              selectedIndex = builder.mSelectedIndex;
            } else if (builder.mSelectedIndices.size() > 0) {
              selectedIndex = builder.mSelectedIndices.get(0);
            }
            if (selectedIndex < 0) {
              return;
            }
            int finalSelectedIndex = selectedIndex;
            recyclerView.post(() -> builder.mLayoutManager.scrollToPosition(finalSelectedIndex));
          }
        });
  }

  public static class Builder extends Popup.Builder {

    protected SmartDialog mDialog;
    protected boolean mAutoDismiss = true;
    protected List<AdjustStyle> mAdjustStyles = new ArrayList<>();

    protected CharSequence mTitleText;
    protected CharSequence mContentText;
    protected CharSequence mDetailText;

    protected CharSequence mPositiveText;
    protected CharSequence mNegativeText;

    protected Uri mIconUri;
    protected Drawable mIcon;

    protected int mInputType = -1;
    protected int mInputMinLength;
    protected int mInputMaxLength;
    protected int mMaxLines = 1;
    protected boolean mInputAllowEmpty = true;
    protected boolean mAlwaysCallInputCallback;
    protected CharSequence mInputPrefill;
    protected CharSequence mInputHint;
    protected SmartDialogInterface.InputCallback mInputCallback;

    protected int mListItemLayout;
    protected int mSelectedIndex = -1;
    protected boolean mAlwaysCallMultiChoiceCallback;
    protected boolean mAlwaysCallSingleChoiceCallback;
    protected List<Integer> mSelectedIndices = new ArrayList<>();
    protected List<CharSequence> mListItems;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected SmartDialogInterface.ListCallback mListCallback;
    protected SmartDialogInterface.ListCallback mListLongCallback;
    protected SmartDialogInterface.ListCallbackMultiChoice mListCallbackMultiChoice;

    protected SmartDialogInterface.ButtonCallback mPositiveCallback;
    protected SmartDialogInterface.ButtonCallback mNegativeCallback;
    protected SmartDialogInterface.ButtonCallback mCloseCallback;

    public Builder(@NonNull Activity activity) {
      super(activity);
      mPopupType = PopupInterface.POPUP_TYPE_DIALOG;
      mExcluded = PopupInterface.Excluded.SAME_TYPE;
      mBackground = new ColorDrawable(0x80000000);
      mInAnimatorCallback = DialogBuilderFactory.getDefaultInAnimator();
      mOutAnimatorCallback = DialogBuilderFactory.getDefaultOutAnimator();
    }

    @Override
    public SmartDialog build() {
      mDialog = new SmartDialog(this);
      return mDialog;
    }

    public <T extends Builder> T addAdjustStyles(@NonNull AdjustStyle adjustStyles) {
      mAdjustStyles.add(adjustStyles);
      return (T) this;
    }

    public <T extends Builder> T addAdjustStyles(@NonNull List<AdjustStyle> adjustStyles) {
      mAdjustStyles.addAll(adjustStyles);
      return (T) this;
    }

    public <T extends Builder> T setTitleText(@StringRes int titleRes, Object... formatArgs) {
      return setTitleText(mActivity.getString(titleRes, formatArgs));
    }

    public <T extends Builder> T setContentText(@StringRes int contentRes, boolean isHtml) {
      CharSequence text = mActivity.getText(contentRes);
      if (isHtml) {
        text = Html.fromHtml(text.toString().replace("\n", "<br/>"));
      }
      return setContentText(text);
    }

    public <T extends Builder> T setContentText(@StringRes int contentRes, Object... formatArgs) {
      String contentText =
          String.format(mActivity.getString(contentRes), formatArgs).replace("\n", "<br/>");
      return setContentText(Html.fromHtml(contentText));
    }

    public <T extends Builder> T setDetailText(@StringRes int detailRes, Object... formatArgs) {
      return setDetailText(mActivity.getString(detailRes, formatArgs));
    }

    public <T extends Builder> T setInput(@StringRes int inputHintRes,
        @StringRes int inputPrefillRes,
        @NonNull SmartDialogInterface.InputCallback inputCallback) {
      return setInput(inputHintRes == 0 ? null : mActivity.getText(inputHintRes),
          inputPrefillRes == 0 ? null : mActivity.getText(inputPrefillRes), inputCallback);
    }

    public <T extends Builder> T setInput(@Nullable CharSequence inputHint,
        @Nullable CharSequence inputPrefill,
        @NonNull SmartDialogInterface.InputCallback inputCallback) {
      mInputHint = inputHint;
      mInputPrefill = inputPrefill;
      mInputCallback = inputCallback;
      return (T) this;
    }

    public <T extends Builder> T inputRange(
        @IntRange(from = 0L, to = Integer.MAX_VALUE) int inputMinLength,
        @IntRange(from = 1L, to = Integer.MAX_VALUE) int inputMaxLength) {
      mInputMinLength = inputMinLength;
      mInputMaxLength = inputMaxLength;
      if (mInputMinLength > 0) {
        mInputAllowEmpty = false;
      }
      return (T) this;
    }

    public <T extends Builder> T setItemsCallback(
        @Nullable SmartDialogInterface.ListCallback listCallback) {
      mListCallback = listCallback;
      return (T) this;
    }

    public <T extends Builder> T setItemsLongCallback(
        @Nullable SmartDialogInterface.ListCallback listLongCallback) {
      mListLongCallback = listLongCallback;
      return (T) this;
    }

    public <T extends Builder> T itemsCallbackMultiChoice(@Nullable List<Integer> selectedIndices,
        @NonNull SmartDialogInterface.ListCallbackMultiChoice callback) {
      if (selectedIndices != null) {
        mSelectedIndices = selectedIndices;
      }
      mListCallbackMultiChoice = callback;
      return (T) this;
    }

    public <T extends Builder> T onPositive(
        @NonNull SmartDialogInterface.ButtonCallback buttonCallback) {
      mPositiveCallback = buttonCallback;
      return (T) this;
    }

    public <T extends Builder> T onNegative(
        @NonNull SmartDialogInterface.ButtonCallback buttonCallback) {
      mNegativeCallback = buttonCallback;
      return (T) this;
    }

    public <T extends Builder> T onClose(
        @NonNull SmartDialogInterface.ButtonCallback buttonCallback) {
      mCloseCallback = buttonCallback;
      return (T) this;
    }

    public SmartDialog getDialog() {
      return mDialog;
    }

    public boolean isAutoDismiss() {
      return mAutoDismiss;
    }

    public <T extends Builder> T setAutoDismiss(boolean autoDismiss) {
      mAutoDismiss = autoDismiss;
      return (T) this;
    }

    public RecyclerView.Adapter getAdapter() {
      return mAdapter;
    }

    public <T extends Builder> T setAdapter(@NonNull RecyclerView.Adapter adapter) {
      mAdapter = adapter;
      return (T) this;
    }

    public SmartDialogInterface.ListCallback getListCallback() {
      return mListCallback;
    }

    public SmartDialogInterface.ListCallback getListLongCallback() {
      return mListLongCallback;
    }

    public SmartDialogInterface.ListCallbackMultiChoice getListCallbackMultiChoice() {
      return mListCallbackMultiChoice;
    }

    public int getListItemLayout() {
      return mListItemLayout;
    }

    public <T extends Builder> T setListItemLayout(@LayoutRes int listItemLayout) {
      mListItemLayout = listItemLayout;
      return (T) this;
    }

    public List<CharSequence> getListItems() {
      return mListItems;
    }

    public <T extends Builder> T setListItems(@ArrayRes int itemsRes) {
      return setListItems(WidgetUtils.getResources().getTextArray(itemsRes));
    }

    public <T extends Builder> T setListItems(@NonNull CharSequence... items) {
      mListItems = new ArrayList<>();
      Collections.addAll(mListItems, items);
      return (T) this;
    }

    public <T extends Builder> T setListItems(@NonNull List<CharSequence> listItems) {
      mListItems = listItems;
      return (T) this;
    }

    public int getSelectedIndex() {
      return mSelectedIndex;
    }

    public <T extends Builder> T setSelectedIndex(int selectedIndex) {
      mSelectedIndex = selectedIndex;
      return (T) this;
    }

    public List<Integer> getSelectedIndices() {
      return mSelectedIndices;
    }

    public boolean isAlwaysCallMultiChoiceCallback() {
      return mAlwaysCallMultiChoiceCallback;
    }

    public <T extends Builder> T setAlwaysCallMultiChoiceCallback(
        boolean alwaysCallMultiChoiceCallback) {
      mAlwaysCallMultiChoiceCallback = alwaysCallMultiChoiceCallback;
      return (T) this;
    }

    public List<AdjustStyle> getAdjustStyles() {
      return mAdjustStyles;
    }

    public CharSequence getTitleText() {
      return mTitleText;
    }

    public <T extends Builder> T setTitleText(@StringRes int titleRes) {
      return setTitleText(mActivity.getText(titleRes));
    }

    public <T extends Builder> T setTitleText(@NonNull CharSequence titleText) {
      mTitleText = titleText;
      return (T) this;
    }

    public CharSequence getContentText() {
      return mContentText;
    }

    public <T extends Builder> T setContentText(@StringRes int contentRes) {
      return setContentText(contentRes, false);
    }

    public <T extends Builder> T setContentText(@NonNull CharSequence contentText) {
      mContentText = contentText;
      return (T) this;
    }

    public CharSequence getDetailText() {
      return mDetailText;
    }

    public <T extends Builder> T setDetailText(@StringRes int detailRes) {
      return setDetailText(mActivity.getText(detailRes));
    }

    public <T extends Builder> T setDetailText(@NonNull CharSequence detailText) {
      mDetailText = detailText;
      return (T) this;
    }

    public CharSequence getPositiveText() {
      return mPositiveText;
    }

    public <T extends Builder> T setPositiveText(@StringRes int positiveRes) {
      return setPositiveText(mActivity.getText(positiveRes));
    }

    public <T extends Builder> T setPositiveText(@NonNull CharSequence positiveText) {
      mPositiveText = positiveText;
      return (T) this;
    }

    public CharSequence getNegativeText() {
      return mNegativeText;
    }

    public <T extends Builder> T setNegativeText(@StringRes int negativeRes) {
      return setNegativeText(mActivity.getText(negativeRes));
    }

    public <T extends Builder> T setNegativeText(@NonNull CharSequence negativeText) {
      mNegativeText = negativeText;
      return (T) this;
    }

    public Uri getIconUri() {
      return mIconUri;
    }

    public <T extends Builder> T setIconUri(@NonNull Uri iconUri) {
      mIconUri = iconUri;
      return (T) this;
    }

    public Drawable getIcon() {
      return mIcon;
    }

    public <T extends Builder> T setIcon(@DrawableRes int icon) {
      return setIcon(mActivity.getResources().getDrawable(icon));
    }

    public <T extends Builder> T setIcon(@NonNull Drawable icon) {
      mIcon = icon;
      return (T) this;
    }

    public int getInputType() {
      return mInputType;
    }

    public <T extends Builder> T setInputType(int inputType) {
      mInputType = inputType;
      return (T) this;
    }

    public int getInputMinLength() {
      return mInputMinLength;
    }

    public int getInputMaxLength() {
      return mInputMaxLength;
    }

    public int getMaxLines() {
      return mMaxLines;
    }

    public <T extends Builder> T setMaxLines(@IntRange(from = 1L) int maxLines) {
      mMaxLines = maxLines;
      return (T) this;
    }

    public boolean isInputAllowEmpty() {
      return mInputAllowEmpty;
    }

    public <T extends Builder> T setInputAllowEmpty(boolean inputAllowEmpty) {
      mInputAllowEmpty = inputAllowEmpty;
      return (T) this;
    }

    public boolean isAlwaysCallInputCallback() {
      return mAlwaysCallInputCallback;
    }

    public <T extends Builder> T setAlwaysCallInputCallback(boolean alwaysCallInputCallback) {
      mAlwaysCallInputCallback = alwaysCallInputCallback;
      return (T) this;
    }

    public CharSequence getInputPrefill() {
      return mInputPrefill;
    }

    public CharSequence getInputHint() {
      return mInputHint;
    }

    public SmartDialogInterface.InputCallback getInputCallback() {
      return mInputCallback;
    }

    public boolean isAlwaysCallSingleChoiceCallback() {
      return mAlwaysCallSingleChoiceCallback;
    }

    public <T extends Builder> T setAlwaysCallSingleChoiceCallback(
        boolean alwaysCallSingleChoiceCallback) {
      mAlwaysCallSingleChoiceCallback = alwaysCallSingleChoiceCallback;
      return (T) this;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
      return mLayoutManager;
    }

    public <T extends Builder> T setLayoutManager(
        @Nullable RecyclerView.LayoutManager layoutManager) {
      mLayoutManager = layoutManager;
      return (T) this;
    }

    public SmartDialogInterface.ButtonCallback getPositiveCallback() {
      return mPositiveCallback;
    }

    public SmartDialogInterface.ButtonCallback getNegativeCallback() {
      return mNegativeCallback;
    }

    public SmartDialogInterface.ButtonCallback getCloseCallback() {
      return mCloseCallback;
    }
  }
}
