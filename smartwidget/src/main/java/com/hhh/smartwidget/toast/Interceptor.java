package com.hhh.smartwidget.toast;

import androidx.annotation.NonNull;

public interface Interceptor {
  @NonNull
  SmartToast.Builder intercept(@NonNull Chain chain);

  interface Chain {
    @NonNull
    SmartToast.Builder request();

    @NonNull
    SmartToast.Builder proceed(@NonNull SmartToast.Builder request);
  }
}
