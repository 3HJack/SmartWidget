package com.hhh.smartwidget.dialog.adjust;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.WidgetUtils;
import com.hhh.smartwidget.dialog.SmartDialog;

public class AdjustTitleLayout implements AdjustStyle {

  @Override
  public void apply(@NonNull SmartDialog dialog) {
    View popupView = dialog.getPopupView();
    TextView titleView = popupView.findViewById(R.id.title);
    if (titleView != null) {
      if (TextUtils.isEmpty(dialog.getBuilder().getContentText())) {
        titleView.setPadding(titleView.getPaddingLeft(), titleView.getPaddingTop(),
            titleView.getPaddingRight(), 0);
      } else if (titleView.getLineCount() > 1) {
        titleView.setPadding(titleView.getPaddingLeft(), titleView.getPaddingTop(),
            titleView.getPaddingRight(),
            WidgetUtils.getDimensionPixelSize(R.dimen.dialog_title_multi_line_padding_bottom));
      }
    }
  }
}
