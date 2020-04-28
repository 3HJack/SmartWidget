package com.hhh.smartwidget.dialog.adjust;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.dialog.SmartDialog;

public class AdjustGeneralStyle implements AdjustStyle {
  @Override
  public void apply(@NonNull SmartDialog dialog) {
    View popupView = dialog.getPopupView();
    TextView contentView = popupView.findViewById(R.id.content);
    if (contentView != null) {
      contentView.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              contentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
              if (contentView.getLineCount() > 3) {
                contentView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
              }
            }
          });
    }

    SmartDialog.Builder builder = dialog.getBuilder();
    if (!TextUtils.isEmpty(builder.getPositiveText())
        || !TextUtils.isEmpty(builder.getNegativeText())) {
      dialog.setCanceledOnTouchOutside(false);
    }

    adjustButtonView(dialog);
  }

  private void adjustButtonView(SmartDialog dialog) {
    View popupView = dialog.getPopupView();
    View positiveView = popupView.findViewById(R.id.positive);
    View negativeView = popupView.findViewById(R.id.negative);
    ViewGroup buttonView = popupView.findViewById(R.id.button);
    if (positiveView != null && negativeView != null && buttonView != null) {
      View verticalDividerView = buttonView.findViewById(R.id.vertical_divider);
      if (verticalDividerView != null) {
        if (positiveView.getVisibility() == View.VISIBLE
            && negativeView.getVisibility() == View.VISIBLE) {
          verticalDividerView.setVisibility(View.VISIBLE);
        } else {
          verticalDividerView.setVisibility(View.GONE);
        }
      }
      View horizontalDividerView = buttonView.findViewById(R.id.horizontal_divider);
      if (horizontalDividerView != null) {
        if (positiveView.getVisibility() == View.GONE
            && negativeView.getVisibility() == View.GONE) {
          horizontalDividerView.setVisibility(View.GONE);
        } else {
          horizontalDividerView.setVisibility(View.VISIBLE);
        }
      }
      if (positiveView.getVisibility() == View.GONE && negativeView.getVisibility() == View.GONE) {
        buttonView.setVisibility(View.GONE);
      }
    }
  }
}
