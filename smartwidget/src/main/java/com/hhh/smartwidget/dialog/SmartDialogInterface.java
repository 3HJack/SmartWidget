package com.hhh.smartwidget.dialog;

import java.util.List;

import android.view.View;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SmartDialogInterface {

  private SmartDialogInterface() {}

  public interface ButtonCallback {
    void onClick(@NonNull SmartDialog dialog, @NonNull View view);
  }

  public interface InputCallback {
    void onInput(@NonNull SmartDialog dialog, @NonNull View view, @NonNull CharSequence input);
  }

  public interface ListCallback {
    void onSelection(@NonNull SmartDialog dialog, @Nullable View itemView,
        @IntRange(from = 0) int position);
  }

  public interface ListCallbackMultiChoice {
    void onSelection(@NonNull SmartDialog dialog,
        @NonNull @IntRange(from = 0) List<Integer> position);
  }
}
